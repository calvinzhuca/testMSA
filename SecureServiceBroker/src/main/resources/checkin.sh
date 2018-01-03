cp Jenkinsfile.groovy Jenkinsfile
cp ReadIdHelper.groovy /var/lib/jenkins/workspace/testPipeline
ls -l /var/lib/jenkins/workspace/testPipeline

git add *
git commit -m "test"
git push
