package com.redhat.refarch.microservices.broker.model;

public class Parameters
{
    private String application_name;

    private String input_url;

    private String application_plan;

    private String service_name;

    public String getApplication_name ()
    {
        return application_name;
    }

    public void setApplication_name (String application_name)
    {
        this.application_name = application_name;
    }

    public String getInput_url ()
    {
        return input_url;
    }

    public void setInput_url (String input_url)
    {
        this.input_url = input_url;
    }

    public String getApplication_plan ()
    {
        return application_plan;
    }

    public void setApplication_plan (String application_plan)
    {
        this.application_plan = application_plan;
    }

    public String getService_name ()
    {
        return service_name;
    }

    public void setService_name (String service_name)
    {
        this.service_name = service_name;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [application_name = "+application_name+", input_url = "+input_url+", application_plan = "+application_plan+", service_name = "+service_name+"]";
    }
}