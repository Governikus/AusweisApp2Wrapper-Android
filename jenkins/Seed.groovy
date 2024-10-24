println 'Try to slurp branches from repository'

def branches = []
def api = new URL("${MERCURIAL_REPOSITORY_URL}/json-branches/")
def content = new groovy.json.JsonSlurper().parse(api.newReader())
content.each
{
    empty, entry -> entry.each
    {
        if(it.status != 'closed')
        {
            branches << it.branch
        }
    }
}
if(branches.isEmpty())
{
	throw new Exception('Cannot find any branch')
}



listView('SDKWrapper') {
    description('SDKWrapper')
    jobs {
        regex(/SDKWrapper.*/)
    }
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}

job("SDKWrapper_Android_Review_Trigger") {
    label('Trigger')
    logRotator {
        daysToKeep(3)
        numToKeep(50)
    }
    parameters {
        stringParam('REVIEWBOARD_SERVER', '', '')
        stringParam('REVIEWBOARD_REVIEW_ID', '', '')
        stringParam('REVIEWBOARD_REVIEW_BRANCH', '', '')
        stringParam('REVIEWBOARD_DIFF_REVISION', '', '')
        stringParam('REVIEWBOARD_STATUS_UPDATE_ID', '', '')
    }
    wrappers {
        preBuildCleanup() {
            deleteDirectories(true)
        }
    }
    steps {
        buildDescription('', '${REVIEWBOARD_REVIEW_ID} / ${REVIEWBOARD_DIFF_REVISION}')
        downstreamParameterized {
            trigger('SDKWrapper_Android_Build') {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
                parameters {
                    predefinedProp('REVIEWBOARD_SERVER', '${REVIEWBOARD_SERVER}')
                    predefinedProp('REVIEWBOARD_REVIEW_ID', '${REVIEWBOARD_REVIEW_ID}')
                    predefinedProp('REVIEWBOARD_REVIEW_BRANCH', '${REVIEWBOARD_REVIEW_BRANCH}')
                    predefinedProp('REVIEWBOARD_DIFF_REVISION', '${REVIEWBOARD_DIFF_REVISION}')
                    predefinedProp('REVIEWBOARD_STATUS_UPDATE_ID', '${REVIEWBOARD_STATUS_UPDATE_ID}')
                    predefinedProp('aarSource', '${REVIEWBOARD_REVIEW_BRANCH}_Android_AAR')
                    predefinedProp('performSonarScan', "true")
                }
            }
        }
        copyArtifacts('SDKWrapper_Android_Build') {
            buildSelector {
                workspace()
            }
        }
    }
    publishers {
        archiveArtifacts {
            allowEmpty(true)
            onlyIfSuccessful(false)
            pattern('**/outputs/apk/**/*.apk')
            pattern('**/outputs/aar/**/*.aar')
            pattern('build/tar/*.tgz')
        }
    }
}

branches.each { branch ->
    job("SDKWrapper_Android_Daily_${branch}") {
        label('Trigger')
        logRotator {
            daysToKeep(7)
            numToKeep(10)
        }
        wrappers {
            preBuildCleanup() {
                deleteDirectories(true)
            }
        }
        steps {
            downstreamParameterized {
                trigger('SDKWrapper_Android_Build') {
                    block {
                        buildStepFailure('FAILURE')
                        failure('FAILURE')
                        unstable('UNSTABLE')
                    }
                    parameters {
                        predefinedProp('REVIEWBOARD_REVIEW_BRANCH', "${branch}")
                        predefinedProp('aarSource', "${branch}_Android_AAR")
                        predefinedProp('performSonarScan', "true")
                        predefinedProp('publish', "snapshot")
                    }
                }
            }
            copyArtifacts('SDKWrapper_Android_Build') {
                buildSelector {
                    workspace()
                }
            }
        }
        publishers {
            archiveArtifacts {
                allowEmpty(false)
                onlyIfSuccessful(true)
                pattern('**/outputs/apk/**/*.apk')
                pattern('**/outputs/aar/**/*.aar')
                pattern('build/tar/*.tgz')
            }
            mailer('autentapp2@governikus.de', true, true)
        }
        triggers {
            cron('0 6 * * *')
        }
    }
}

job("SDKWrapper_Android_Release") {
    label('Trigger')
    parameters {
        stringParam( 'changeset', '', 'Build given changeset (tag) as release' )
        choiceParam( 'aarSource', ['maven', 'Release_Android_AAR'], 'Source of the AAR.')
        reactiveChoice {
            name ('actions')
            description('Upload to maven central repository')
            filterable(false)
            choiceType('PT_CHECKBOX')
            script {
                groovyScript {
                    script {
                        script("if (aarSource.equals('maven')) { return ['release:selected', 'central'] } else { return ['release:selected', 'central:disabled'] }")
                        sandbox(true)
                    }
                    fallbackScript {
                        script("return ['SCRIPT ERROR:disabled']")
                        sandbox(true)
                    }
                }
            }
            referencedParameters('aarSource')
            randomName('')
            filterLength(0)
        }
    }
    wrappers {
        preBuildCleanup() {
            deleteDirectories(true)
        }
    }
    steps {
        wrappers {
            buildName('${changeset}')
        }
        buildDescription('', '${aarSource}')
        downstreamParameterized {
            trigger('SDKWrapper_Android_Build') {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
                parameters {
                    predefinedProp('publish', '$actions')
                    predefinedProp('REVIEWBOARD_REVIEW_BRANCH', '$changeset')
                    predefinedProp('aarSource', '$aarSource')
                }
            }
        }
        copyArtifacts('SDKWrapper_Android_Build') {
            buildSelector {
                workspace()
            }
        }
    }
    publishers {
        archiveArtifacts {
            allowEmpty(false)
            onlyIfSuccessful(true)
            pattern('dist/**/*')
            pattern('build/tar/*.tgz')
            pattern('**/outputs/apk/**/*.apk')
        }
    }
}

pipelineJob('SDKWrapper_Android_Build') {
    logRotator {
        daysToKeep(7)
        numToKeep(50)
    }
    definition {
        cps {
            script(readFileFromWorkspace('jenkins/PipelineBuild.groovy'))
            sandbox()
        }
    }
}
