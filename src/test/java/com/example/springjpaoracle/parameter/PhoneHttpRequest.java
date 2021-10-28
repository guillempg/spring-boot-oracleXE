package com.example.springjpaoracle.parameter;

public class PhoneHttpRequest
{
    final String phoneNumber;


    public PhoneHttpRequest(final String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }
}
