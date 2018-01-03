
def say() {
    // Hello World
    echo "Hello world1!"
    return "Hello world2!"
}

def getServiceId(String inputString) {
    String serviceId = ""
    if (null != inputString && "" != inputString){
        if (inputString.indexOf("<service_id>") > -1){
            serviceId = inputString.substring(inputString.indexOf("<service_id>") + "<service_id>".length(), inputString.indexOf("</service_id>"));
        }
    }
    return serviceId
}    

return this;
    

