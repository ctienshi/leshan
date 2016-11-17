package org.eclipse.leshan.client.demo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class RunShellCommandFromJava {

    public static String getFirmware() {

        String command = "uname -a";

        try {
            Process proc = Runtime.getRuntime().exec(command);
            BufferedReader reader =  
            new BufferedReader(new InputStreamReader(proc.getInputStream()));
            
            String details = "";
            details = reader.readLine();
            //System.out.print(line + "");
            return details;

        } catch (IOException ex){
            System.out.println(ex);
        }
    return null;
    }

    public static String getManufacturer() {
        String command = "uname -n";

        try {
            Process proc = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String manufacturer = "";
            manufacturer = reader.readLine();

            return manufacturer;

        } catch (IOException ex){
            System.out.println(ex);
        }
        return null;
    }
}
