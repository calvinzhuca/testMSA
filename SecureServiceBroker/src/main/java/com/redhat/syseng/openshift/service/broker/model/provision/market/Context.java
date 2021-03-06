package com.redhat.syseng.openshift.service.broker.model.provision.market;

public class Context
{
    private String platform;

    private String namespace;

    public String getPlatform ()
    {
        return platform;
    }

    public void setPlatform (String platform)
    {
        this.platform = platform;
    }

    public String getNamespace ()
    {
        return namespace;
    }

    public void setNamespace (String namespace)
    {
        this.namespace = namespace;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [platform = "+platform+", namespace = "+namespace+"]";
    }
}
