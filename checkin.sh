cp /home/czhu/works/git/testMSA/SecureServiceBroker/src/main/resources/Jenkinsfile.groovy Jenkinsfile
cp /home/czhu/works/git/testMSA/SecureServiceBroker/src/main/resources/ReadIdHelper.groovy ReadIdHelper.groovy
ls -l /var/lib/jenkins/workspace/testPipeline

git add *
git commit -m "test"
git push
