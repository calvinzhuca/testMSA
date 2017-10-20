package com.redhat.syseng.openshift.service.broker.model.provision.market;

public class Parameters
{
    private String applicationName;

    private String description;



    public String getApplicationName ()
    {
        return applicationName;
    }

    public void setApplicationName (String name)
    {
        this.applicationName = name;
    }

    public String getDescription ()
    {
        return description;
    }

    public void setDescription (String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [applicationName = "+applicationName+", description = "+description+"]";
    }
}