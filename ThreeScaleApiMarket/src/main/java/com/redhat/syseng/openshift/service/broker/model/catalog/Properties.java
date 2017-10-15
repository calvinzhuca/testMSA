/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.syseng.openshift.service.broker.model.catalog;

public class Properties
{
    private Username username;

    private Email email;

    private Password password;

    public Username getUsername ()
    {
        return username;
    }

    public void setUsername (Username username)
    {
        this.username = username;
    }

    public Email getEmail ()
    {
        return email;
    }

    public void setEmail (Email email)
    {
        this.email = email;
    }

    public Password getPassword ()
    {
        return password;
    }

    public void setPassword (Password password)
    {
        this.password = password;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [username = "+username+", email = "+email+", password = "+password+"]";
    }
}
