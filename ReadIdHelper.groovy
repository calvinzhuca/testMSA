
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

def getServiceId2(String inputString) {
    String serviceId = ""
    if (null != inputString && "" != inputString){
        if (inputString.indexOf("<service><id>") > -1){
            //skip the first one, which is "echo"
            int j = inputString.indexOf("<service><id>") + "<service><id>".length()
            println("new j: " + j)
            inputString = inputString.substring(j)
            println("new inputString: " + inputString)
            serviceId = inputString.substring(inputString.indexOf("<service><id>") + "<service><id>".length(), inputString.indexOf("</id>"));
        }
    }
    return serviceId
}  

def getPlanId(String inputString) {
    String planId = ""
    if (null != inputString && "" != inputString){
        if (inputString.indexOf("<id>") > -1){
            planId = inputString.substring(inputString.indexOf("<id>") + "<id>".length(), inputString.indexOf("</id>"));
        }
    }
    return planId
} 

return this;
    

