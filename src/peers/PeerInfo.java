package peers;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class PeerInfo {

    private static Integer cookie_count = 1;

    //hostname of the peer
    private String hostName;

    //one peer register, it will have a cookie
    private Integer cookie;

    // the peer is currently active or not
    private Boolean isActive;

    //TTL field
    private Integer TTL;

    //port number to which the RFC server of this peer is listening
    //valid only if the peer is active
    private Integer port;

    //# of times that the peer has been active during the last 30 days
    private Integer registered_count;

    //most recent time the peer registered
    private String last_registered;

    //pointer
    Peer next;

    public PeerInfo(String hostName, Integer port) {
        this.hostName = hostName;
        this.cookie = PeerInfo.cookie_count;
        this.isActive = true;
        this.TTL = 7200;
        this.port = port;
        this.registered_count = 1;
        this.last_registered = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(new Date().getTime());
        PeerInfo.cookie_count += 1;
    }


    public void update_reg_count() {
        setRegistered_count(getRegistered_count() + 1);
    }

    public void renew_register() {
        update_reg_count();
        setLast_registered(new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(new Date().getTime()));
    }

    public static long getTimeDiff(String paramTime) {
        // get system time
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String systemTime = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(new Date().getTime());
        long difference = 0;

        try {
            Date systemDate = dateFormat.parse(systemTime);
            Date paramDate = dateFormat.parse(paramTime);
            difference = systemDate.getTime() - paramDate.getTime();
//            System.out.println("time difference is " + difference + "ms");

        } catch (ParseException p) {
            p.printStackTrace();
        }
        return difference;
    }


    public void initialize_ttl() {
        setTTL(7200);
        setActive(true);
    }

    public boolean is_active() {
        return getActive();
    }

    public void mark_inactive() {
        setActive(false);
    }

    public void decrementTTL(int decrement_value) {
        if (TTL > 0) {
            setTTL(TTL - decrement_value);
            if (TTL <= 0) {
                setTTL(0);
                mark_inactive();
            }
        }
    }


    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Integer getCookie() {
        return cookie;
    }

    public void setCookie(Integer cookie) {
        this.cookie = cookie;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Integer getTTL() {
        return TTL;
    }

    public void setTTL(Integer TTL) {
        this.TTL = TTL;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getRegistered_count() {
        return registered_count;
    }

    public void setRegistered_count(Integer registered_count) {
        this.registered_count = registered_count;
    }

    public String getLast_registered() {
        return last_registered;
    }

    public void setLast_registered(String last_registered) {
        this.last_registered = last_registered;
    }
}
