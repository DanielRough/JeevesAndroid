package com.example.daniel.jeeves;

/**
 * Created by Daniel on 30/09/2017.
 */

public class Person {


    public static void network(Person p1, Person p2){

    }
    public void setName(String name){

    }
    public void setJob(String name){

    }
    public void setPhone(String name){

    }
    public void setMail(String name){

    }
    public void contact(Person p){
    Person.businessCard();
    }


    private static void businessCard()
    {
        Person me = new SoftwareDeveloper();
        me.setName("Daniel Rough");
        me.setMail("daniel.j.rough@gmail.com");
        me.setPhone("07585552201");
        Person you = new Contact();
        if (you.like(me))
        {
            //TODO: Let's stay in touch!
            Person.network(me,you);
        }
    }

    private boolean like(Person me) {
        if(me.equals(new Person()))
        return true;
        return false;
    }
}
