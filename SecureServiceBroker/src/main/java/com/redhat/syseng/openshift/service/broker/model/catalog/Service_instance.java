/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.syseng.openshift.service.broker.model.catalog;

public class Service_instance
{
    //update structure is the same as create
    private Create update;

    private Create create;

    public Create getUpdate ()
    {
        return update;
    }

    public void setUpdate (Create update)
    {
        this.update = update;
    }

    public Create getCreate ()
    {
        return create;
    }

    public void setCreate (Create create)
    {
        this.create = create;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [update = "+update+", create = "+create+"]";
    }
}