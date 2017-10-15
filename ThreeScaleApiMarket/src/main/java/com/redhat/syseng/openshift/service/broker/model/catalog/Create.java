/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.syseng.openshift.service.broker.model.catalog;

import com.redhat.syseng.openshift.service.broker.model.provision.Parameters;

public class Create
{
    private Parameters parameters;

    public Parameters getParameters ()
    {
        return parameters;
    }

    public void setParameters (Parameters parameters)
    {
        this.parameters = parameters;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [parameters = "+parameters+"]";
    }
}