package com.example.ozgurozdemir.meetpoint;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

public class Database {

    private String path;
    private SQLiteDatabase database;

    public Database(String path){
        this.path = path;
    }

    // Create database with object will used after
    public void createDB(){
        try{
            // Open database
            database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.CREATE_IF_NECESSARY);
            // Create Person entity
            String create = "CREATE TABLE IF NOT EXISTS Person(" +
                    "person_id text PRIMARY KEY," +
                    "person_username text," +
                    "person_password text," +
                    "person_name text," +
                    "person_mail text," +
                    "person_phone int," +
                    "person_office int);";
            database.execSQL(create);
            // Create Meeting entity
            create = "CREATE TABLE IF NOT EXISTS Meeting(" +
                    "meeting_id text PRIMARY KEY," +
                    "meeting_name text," +
                    "meeting_loc text," +
                    "meeting_date datetime," +
                    "meeting_note text);";
            database.execSQL(create);
            // Create Friendship entity
            create = "CREATE TABLE IF NOT EXISTS Friendship(" +
                    "friendship_id text PRIMARY KEY," +
                    "person_id text," +
                    "friend_id text);";
            database.execSQL(create);
            // Create Schedule entity
            create = "CREATE TABLE IF NOT EXISTS Schedule(" +
                    "schedule_id text PRIMARY KEY," +
                    "person_id text," +
                    "meeting_id text);";
            database.execSQL(create);

            database.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    // Registering database
    // It returns only 1 and -1
    // Return 1 for successfully created account
    // Return -1 for username is already taken
    public int registerDB(String username, String password, String name, String mail, double phone, double office){

        try{
            database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.CREATE_IF_NECESSARY);
            String id = "person_" + username;
            // Looking for username is taken or not
            String sql = "select * from Person";
            Cursor c = database.rawQuery(sql, null);
            c.moveToPosition(-1);
            while ( c.moveToNext() ){
                String _ = c.getString(0);
                if(_.equals(id)){
                    return -1;
                }
            }
            // Registering account to database
            String register = "INSERT INTO Person VALUES('" +
                    id + "','" + username +
                    "','" + password + "','" + name + "','" +
                    mail + "'," + phone + "," + office + ");";
            database.execSQL(register);

            database.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    // Login database method that returns user_id and user_name in order to use
    public String[] loginDB(String username, String password){
        String[] loginInformation = {"",""};
        try{
            database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.CREATE_IF_NECESSARY);
            String sql = "select * from Person";
            Cursor c = database.rawQuery(sql, null);
            c.moveToPosition(-1);
            while ( c.moveToNext() ){
                String u = c.getString(1);
                String p = c.getString(2);
                if(username.equals(u) && password.equals(p)){
                    loginInformation[0] = c.getString(0);
                    loginInformation[1] = c.getString(3);
                    return loginInformation;
                }
            }
            database.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loginInformation;
    }

    // Getting upcoming event information with respect to userID
    public String getUpcomingEvent(String userID){
        try{
            database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.CREATE_IF_NECESSARY);
            String sql = "SELECT * FROM Schedule INNER JOIN Meeting ON Schedule.meeting_id=Meeting.meeting_id WHERE person_id='" +
                    userID + "' ORDER BY meeting_date ASC;";
            Cursor c = database.rawQuery(sql, null);
            // If user have got meetings, then take first meeting
            if(c.getCount()!=0){
                c.moveToPosition(0);
                String[] _ = c.getString(6).split(" ");
                String[] date = _[0].split("-");
                String hour = _[1];
                String information = hour + "\n" + date[2]+ "/" + date[1] + "/" + date[0] + "\n" + c.getString(4);
                return information;
            }
            database.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Otherwise return empty string
        return "";
    }

    // Getting events information with respect to userID
    public ArrayList<Meeting> getEvents(String userID){
        ArrayList<Meeting> result = new ArrayList<Meeting>();
        try{
            database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.CREATE_IF_NECESSARY);
            String sql = "SELECT * FROM Schedule INNER JOIN Meeting ON Schedule.meeting_id=Meeting.meeting_id WHERE person_id='" +
                   userID + "' ORDER BY meeting_date ASC;";
            Cursor c = database.rawQuery(sql, null);
            c.moveToPosition(-1);
            while (c.moveToNext()){
                result.add(new Meeting(c.getString(3), c.getString(4), c.getString(5),
                        c.getString(6), c.getString(7)));
            }
            database.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Getting friends id and name information with respect to userID
    public ArrayList<String> getFriends(String userID){
        ArrayList<String> result = new ArrayList<String>();
        try{
            database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.CREATE_IF_NECESSARY);
            String sql = "SELECT * FROM Person INNER JOIN Friendship ON Friendship.friend_id=Person.person_id " +
                    "WHERE Friendship.person_id='" + userID + "';";
            Cursor c = database.rawQuery(sql, null);
            c.moveToPosition(-1);
            while (c.moveToNext() ){
                // Taking id and name information and assign it to string with separator '-'
                result.add(c.getString(3) + "-" + c.getString(0));
            }

            database.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Getting all people id and name information which not included userID
    public ArrayList<String> getPeople(String userID){
        ArrayList<String> result = new ArrayList<String>();
        try{
            database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.CREATE_IF_NECESSARY);
            String sql = "SELECT Person.person_id, Person.person_name FROM Person WHERE person_id!='" + userID + "';";
            Cursor c = database.rawQuery(sql, null);
            c.moveToPosition(-1);
            while (c.moveToNext() ){
                result.add(c.getString(1) + "-" + c.getString(0));
            }
            // If given user is friend with that person, then subtract person from the list
            ArrayList<String> diff = new ArrayList<String>();
            sql = "SELECT Friendship.friend_id FROM Friendship WHERE person_id='" + userID + "';";
            c = database.rawQuery(sql, null);
            c.moveToPosition(-1);
            while (c.moveToNext() ){
                diff.add(c.getString(0));
            }
            for(int i = result.size()-1; i >=0 ; i--){
                if(diff.contains(result.get(i).split("-")[1]))
                    result.remove(i);
            }

            database.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Getting given person object friend with user in order to view detailed information
    public Person getFriend(String userID, String friendID){
        Person person = new Person("","","","");
        try{
            database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.CREATE_IF_NECESSARY);
            String sql = "SELECT * FROM Person INNER JOIN Friendship ON Friendship.friend_id=Person.person_id " +
                    "WHERE Friendship.person_id='" + userID + "' AND Friendship.friend_id='" + friendID + "';";
            Cursor c = database.rawQuery(sql, null);
            c.moveToPosition(0);
            person.setId(friendID);
            person.setName(c.getString(3));
            person.setMail(c.getString(4));
            person.setPhone(c.getString(5));
            person.setOffice(c.getString(6));
            database.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return person;
    }

    // Getting given person object in order to view detailed information
    public Person getPerson(String userID){
        Person person = new Person("","","","");
        try{
            database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.CREATE_IF_NECESSARY);
            String sql = "SELECT * FROM Person WHERE person_id='" + userID + "';";
            Cursor c = database.rawQuery(sql, null);
            c.moveToPosition(0);
            person.setId(userID);
            person.setName(c.getString(3));
            person.setMail(c.getString(4));
            person.setPhone(c.getString(5));
            person.setOffice(c.getString(6));
            database.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return person;
    }

    // Adding friendship with given user and given friend
    public void addFriend(String userID, String friendID){
        try{
            database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.CREATE_IF_NECESSARY);
            String sql = "INSERT INTO Friendship VALUES ('friendship_" + userID + "_" + friendID + "','" +
                    userID + "','" + friendID + "');";
            database.execSQL(sql);
            sql = "INSERT INTO Friendship VALUES ('friendship_" + friendID + "_" + userID + "','" +
                    friendID + "','" + userID + "');";
            database.execSQL(sql);
            database.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    // Delete friendship given user and given friend
    public void deleteFriend(String userID, String friendID){
        try{
            database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.CREATE_IF_NECESSARY);
            String sql = "DELETE FROM Friendship WHERE friend_id='" + friendID + "' AND person_id='" + userID + "';";
            database.execSQL(sql);
            database.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    // Scheduling meeting (creating meeting object)
    public void scheduleMeeting(String userID, String name,String location, String hour, String date,
                          ArrayList<String> participants, String note){
        try{
            database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.CREATE_IF_NECESSARY);

            // Creating meeting object
            String[] date_format = date.split("/");
            String meeting_date = date_format[2] + "-" + date_format[1] + "-" + date_format[0] + " " + hour;
            String meeting_id = "meeting_" + userID + "_" + meeting_date;
            String sql = "INSERT INTO Meeting VALUES('" + meeting_id + "','" + name + "','" +
                    location + "','" + meeting_date + "','" + note + "');";
            database.execSQL(sql);

            // Creating meeting object at schedule table for user
            sql = "INSERT INTO Schedule VALUES('schedule_" + userID + "_" + meeting_id + "','" +
                    userID + "','" + meeting_id + "');";
            database.execSQL(sql);

            // Creating meeting object at schedule table for participants
            for(String s: participants){
                String id = s.split("-")[1];
                sql = "INSERT INTO Schedule VALUES('schedule_" + id + "_" + meeting_id + "','" +
                        id + "','" + meeting_id + "');";
                database.execSQL(sql);
            }

            database.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    // Canceling meeting (delete meeting object)
    public void cancelMeeting(String meetingID){
        try{
            database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.CREATE_IF_NECESSARY);
            // Delete meeting from meeting table
            String sql = "DELETE FROM Meeting WHERE meeting_id='" + meetingID + "';";
            database.execSQL(sql);
            // Delete meeting from schedule which means removing meeting from all participants
            sql = "DELETE FROM Schedule WHERE meeting_id='" + meetingID + "';";
            database.execSQL(sql);

            database.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    // Get all participants of meeting and return in specific string to use easily
    public String getParticipants(String meetingID){
        String result = "";
        try{
            database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.CREATE_IF_NECESSARY);
            // Selecting all participants
            String sql = "SELECT Schedule.person_id FROM Meeting INNER JOIN Schedule ON Meeting.meeting_id=Schedule.meeting_id " +
                    "WHERE Schedule.meeting_id='" + meetingID + "';";
            Cursor c = database.rawQuery(sql, null);
            c.moveToPosition(-1);
            while (c.moveToNext() ){
                // Getting all participants name
                sql = "SELECT person_name FROM Person WHERE person_id='" + c.getString(0) + "';";
                Cursor c1 = database.rawQuery(sql, null);
                c1.moveToPosition(0);
                result += c1.getString(0) + ", ";
            }
            // Removing the last separator
            result = result.substring(0, result.length()-2);
            database.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Get all participants' phone number of meeting and return in specific string to use easily
    public String getParticipantsPhoneNumber(String userID, String meetingID){
        String result = "";
        try{
            database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.CREATE_IF_NECESSARY);
            // Selecting all participants
            String sql = "SELECT Schedule.person_id FROM Meeting INNER JOIN Schedule ON Meeting.meeting_id=Schedule.meeting_id " +
                    "WHERE Schedule.meeting_id='" + meetingID + "' AND Schedule.person_id!='" + userID + "';";
            Cursor c = database.rawQuery(sql, null);
            c.moveToPosition(-1);
            while (c.moveToNext() ){
                // Getting all participants phone number
                sql = "SELECT person_phone FROM Person WHERE person_id='" + c.getString(0) + "';";
                Cursor c1 = database.rawQuery(sql, null);
                c1.moveToPosition(0);
                result += c1.getString(0) + ";";
            }
            // Removing the last separator
            if (!result.isEmpty())
                result = result.substring(0, result.length()-1);
            database.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Get all participants' mail of meeting and return in specific string to use easily
    public String getParticipantsMailAddresses(String userID, String meetingID){
        String result = "";
        try{
            database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.CREATE_IF_NECESSARY);
            // Selecting all participants
            String sql = "SELECT Schedule.person_id FROM Meeting INNER JOIN Schedule ON Meeting.meeting_id=Schedule.meeting_id " +
                    "WHERE Schedule.meeting_id='" + meetingID + "' AND Schedule.person_id!='" + userID + "';";
            Cursor c = database.rawQuery(sql, null);
            c.moveToPosition(-1);
            while (c.moveToNext() ){
                // Getting all participants mail
                sql = "SELECT person_mail FROM Person WHERE person_id='" + c.getString(0) + "';";
                Cursor c1 = database.rawQuery(sql, null);
                c1.moveToPosition(0);
                result += c1.getString(0) + ";";
            }
            // Removing the last separator
            if(!result.isEmpty())
                result = result.substring(0, result.length()-1);
            database.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


}
