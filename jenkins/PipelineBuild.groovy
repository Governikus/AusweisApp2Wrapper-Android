pipeline {
    agent {
        label 'Android'
    }
    parameters {
        string( name: 'REVIEWBOARD_REVIEW_ID', defaultValue: '', description: 'ID of the Review' )
        string( name: 'REVIEWBOARD_REVIEW_BRANCH', defaultValue: 'default', description: 'Branch/Revision' )
        string( name: 'REVIEWBOARD_SERVER', defaultValue: '', description: 'Server' )
        string( name: 'REVIEWBOARD_STATUS_UPDATE_ID', defaultValue: '', description: '' )
        string( name: 'REVIEWBOARD_DIFF_REVISION', defaultValue: '', description: '' )
        booleanParam( name: 'performSonarScan', defaultValue: false, description: 'Perform a sonar scan')
        string( name: 'publish', defaultValue: '', description: 'Publish (snapshot | release | central) repository')
        string( name: 'aarSource', defaultValue: 'maven', description: 'Source of the AAR.\nExamples: maven, Release_Android_AAR, default_Review_Android_AAR, default_Android_AAR' )
    }
    options {
        skipStagesAfterUnstable()
        disableConcurrentBuilds()
        timeout(time: 30, unit: 'MINUTES')
    }
    stages {
        stage('Cleanup') {
            steps {
                cleanWs(
                    deleteDirs: true,
                    disableDeferredWipeout: true
                )
            }
        }
        stage('Checkout') {
            steps {
                checkout( [
                    $class : 'MercurialSCM',
                    revisionType: 'TAG',
                    revision: "${params.REVIEWBOARD_REVIEW_BRANCH}",
                    clean  : true,
                    source : 'https://hg.governikus.de/AusweisApp/SDKWrapper-Android'
                ] )
            }
        }
        stage('Copy AAR') {
            when {
                expression {
                    return params.aarSource != 'maven';
                }
            }

            steps {
                copyArtifacts(
                    projectName: "${params.aarSource}",
                    filter: '**/*.aar',
                    flatten: true,
                    target: './ausweisapp',
                    selector: lastSuccessful()
                )
                script {
                    currentBuild.description = "${params.aarSource}"
                    sh 'mv ./ausweisapp/ausweisapp*.aar ./ausweisapp/ausweisapp.aar'
                }
            }
        }
        stage('Patch') {
            when { expression { params.REVIEWBOARD_REVIEW_ID != '' } }
            steps {
                publishReview downloadOnly: true, installRBTools: false
                sh "hg --config patch.eol=auto import --no-commit patch.diff"
            }
        }
        stage('Static analysis') {
            steps {
                sh './gradlew lintKotlin'
                sh './gradlew lint'
                recordIssues (
                    tool: androidLintParser(pattern: '**/lint-results*.xml'),
                    qualityGates: [[threshold: 2, type: 'TOTAL', unstable: false]]
                )
            }
        }
        stage('Compile') {
            steps {
                sh './gradlew compileDebugSources'
            }
        }
        stage('Unit test') {
            steps {
                sh './gradlew testDebugUnitTest'
                sh './gradlew compileDebugAndroidTestKotlin'
                junit '**/TEST-*.xml'
            }
        }
        stage('Sonar') {
            when { expression { params.performSonarScan } }
            steps {
                script {
                    def pullRequestParams = params.REVIEWBOARD_REVIEW_ID != '' ? '-Dsonar.pullrequest.key=${REVIEWBOARD_REVIEW_ID} -Dsonar.pullrequest.branch=${REVIEWBOARD_REVIEW_ID} -Dsonar.pullrequest.base=${REVIEWBOARD_REVIEW_BRANCH}' : '-Dsonar.branch.name=${REVIEWBOARD_REVIEW_BRANCH}'
                    sh "./gradlew sonar -Dsonar.plugins.downloadOnlyRequired=false -Dsonar.scanner.metadataFilePath=${WORKSPACE}/tmp/sonar-metadata.txt -Dsonar.projectName=AusweisApp-SDKWrapper-Android ${pullRequestParams} -Dsonar.token=${SONARQUBE_TOKEN} -Dsonar.qualitygate.wait=true -Dsonar.qualitygate.timeout=90"
                }
            }
        }
        stage('Package') {
            steps {
                sh './gradlew assemble'
                sh "./gradlew -Dmaven.repo.local=${WORKSPACE}/dist publishReleasePublicationToMavenLocal"
            }
        }
        stage('Publish snapshot') {
            when { expression { params.publish.contains('snapshot') } }
            steps {
                sh './gradlew publishSnapshotPublicationToNexusSnapshotRepository'
            }
        }
        stage('Publish release') {
            when { expression { params.publish.contains('release') } }
            steps {
                sh './gradlew publishReleasePublicationToNexusReleaseRepository'
            }
        }
        stage('Publish Maven') {
            when {
                expression { params.publish.contains('central') }
                equals expected: 'maven', actual: params.aarSource
            }
            steps {
                sh './gradlew publishReleasePublicationToCentralRepository'
            }
        }
        stage('Tarball') {
            steps {
                sh './gradlew tarball'
            }
        }
        stage('Archive') {
            steps {
                archiveArtifacts artifacts: '**/outputs/apk/**/*.apk', allowEmptyArchive: true
                archiveArtifacts artifacts: '**/outputs/aar/**/*.aar'
                archiveArtifacts artifacts: 'dist/**/*', excludes: '**/*.xml', allowEmptyArchive: true
                archiveArtifacts artifacts: 'build/tar/*.tgz'
            }
        }
    }

    post {
        always {
            script {
                if (params.REVIEWBOARD_REVIEW_ID != '') {
                    def rb_result = "error"
                    def rb_desc = "build failed."
                    if (currentBuild.result == 'SUCCESS') {
                        rb_result = "done-success"
                        rb_desc = "build succeeded."
                    }

                    withCredentials([string(credentialsId: 'RBToken', variable: 'RBToken')]) {
                        sh "rbt status-update set --state ${rb_result} --description '${rb_desc}' -r ${params.REVIEWBOARD_REVIEW_ID} -s ${params.REVIEWBOARD_STATUS_UPDATE_ID} --server ${params.REVIEWBOARD_SERVER} --username jenkins --api-token $RBToken"
                    }
                }
            }
        }
    }
}
