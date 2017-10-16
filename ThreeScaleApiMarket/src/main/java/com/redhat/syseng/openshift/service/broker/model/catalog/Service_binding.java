/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.syseng.openshift.service.broker.model.catalog;

public class Service_binding
{
    private String create = "{\"parameters\":{\"$schema\":\"http://json-schema.org/draft-04/schema\",\"additionalProperties\":false,\"properties\":{\"service_name\":{\"default\":\"testapi\",\"title\":\"3scale service name\",\"type\":\"string\"}}}}";

    public String getCreate ()
    {
        return create;
    }

    public void setCreate (String create)
    {
        this.create = create;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [create = "+create+"]";
    }
}
