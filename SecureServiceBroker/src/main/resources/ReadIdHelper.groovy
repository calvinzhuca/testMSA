package utilities

class ReadIdHelper {
    static String say() {
        // Hello World
        return "Hello world!"
    }
    
    static String getServiceId(def inputString) {
        String serviceId = ""
        if (null != inputString && "" != inputString){
            if (inputString.indexOf("<service_id>") > -1){
                serviceId = inputString.substring(inputString.indexOf("<service_id>") + "<service_id>".length(), inputString.indexOf("</service_id>"));
            }
        }
        return serviceId
    }    
}
