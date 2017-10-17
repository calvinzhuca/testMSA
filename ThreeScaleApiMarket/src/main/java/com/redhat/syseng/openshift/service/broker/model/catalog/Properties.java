/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.syseng.openshift.service.broker.model.catalog;

public class Properties
{
    private ApplicationName applicationName;
    private Description description;

    public ApplicationName getApplicationName ()
    {
        return applicationName;
    }

    public void setApplicationName (ApplicationName applicationName)
    {
        this.applicationName = applicationName;
    }


    public Description getDescription ()
    {
        return description;
    }

    public void setDescription (Description description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [applicationName = "+applicationName+", description = "+description+"]";
    }
}
