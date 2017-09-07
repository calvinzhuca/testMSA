package com.redhat.refarch.microservices.broker.model;

public class Parameters
{
    private String amp_admin_pass;

    private String amp_admin_user;

    private String amp_url;

    public String getAmp_admin_pass ()
    {
        return amp_admin_pass;
    }

    public void setAmp_admin_pass (String amp_admin_pass)
    {
        this.amp_admin_pass = amp_admin_pass;
    }

    public String getAmp_admin_user ()
    {
        return amp_admin_user;
    }

    public void setAmp_admin_user (String amp_admin_user)
    {
        this.amp_admin_user = amp_admin_user;
    }

    public String getAmp_url ()
    {
        return amp_url;
    }

    public void setAmp_url (String amp_url)
    {
        this.amp_url = amp_url;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [amp_admin_pass = "+amp_admin_pass+", amp_admin_user = "+amp_admin_user+", amp_url = "+amp_url+"]";
    }
}
