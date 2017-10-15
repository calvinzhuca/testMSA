/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.syseng.openshift.service.broker.model.catalog;

/**
 *
 * @author czhu
 */
public class Catalog {
    private Service[] services;

    public Service[] getServices ()
    {
        return services;
    }

    public void setServices (Service[] services)
    {
        this.services = services;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [services = "+services+"]";
    }    
}
