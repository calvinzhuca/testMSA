/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.syseng.openshift.service.broker.model.catalog;

public class Schemas
{
    private Service_binding service_binding;

    private Service_instance service_instance;

    public Service_binding getService_binding ()
    {
        return service_binding;
    }

    public void setService_binding (Service_binding service_binding)
    {
        this.service_binding = service_binding;
    }

    public Service_instance getService_instance ()
    {
        return service_instance;
    }

    public void setService_instance (Service_instance service_instance)
    {
        this.service_instance = service_instance;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [service_binding = "+service_binding+", service_instance = "+service_instance+"]";
    }
}
	