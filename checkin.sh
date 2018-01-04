cp /home/czhu/works/git/testMSA/SecureServiceBroker/src/main/resources/Jenkinsfile.groovy Jenkinsfile
cp /home/czhu/works/git/testMSA/SecureServiceBroker/src/main/resources/ReadIdHelper.groovy ReadIdHelper.groovy


git add *
git commit -m "test"
git push

cd /var/lib/jenkins/workspace/BrokerPipeline/
rm -rf *
ls -l

