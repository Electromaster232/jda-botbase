package me.djelectro.snipebot.types;

import me.djelectro.snipebot.Database;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

public class Snipe {

    private int id = 0;

    public SnipePlayer getSniper() {
        return sniper;
    }

    public SnipePlayer getSniped() {
        return sniped;
    }

    public String getAttachUrl() {
        return attachUrl;
    }

    public Date getTime() {
        return time;
    }

    public String getMessage(){return message;}

    public SnipeGuild getGuild(){return guild;}

    private SnipePlayer sniper;
    private SnipePlayer sniped;
    private String attachUrl;
    private Date time;

    private SnipeGuild guild;

    private String message;


    public Snipe(SnipePlayer sniper, SnipePlayer sniped, String attachUrl, Date time, SnipeGuild g){
        this.sniped = sniped;
        this.sniper = sniper;
        this.attachUrl = attachUrl;
        this.time = time;
        this.guild = g;
    }

    public Snipe(SnipePlayer sniper, SnipePlayer sniped, String attachUrl, Date time, SnipeGuild g, String message){
        this.sniped = sniped;
        this.sniper = sniper;
        this.attachUrl = attachUrl;
        this.time = time;
        this.message = message;
        this.guild = g;
    }

    // If we only have the ID, we should try to retrieve the rest of the information from the database
    public Snipe(int id, JDA bot){
        String[] row = Database.getInstance().executeAndReturnData("SELECT * FROM snipes WHERE id = ?", id).entrySet().iterator().next().getValue();
        this.sniper = new SnipePlayer(bot.getUserById(row[1]));
        this.sniped = new SnipePlayer(bot.getUserById(row[2]));
        this.attachUrl = row[3];
        this.time = stringToDate(row[4]);
        this.message = row[5];
    }

    public int getId(){return id;}

    public static Snipe createSnipe(CommandInteractionPayload event){
        OptionMapping m = event.getOption("member");
        if(m == null) {
            return null;
        }
        User mA = m.getAsUser();

        OptionMapping oMI = event.getOption("attachment");
        if(oMI == null){
            return null;
        }
        Message.Attachment attachment = oMI.getAsAttachment();

        OptionMapping msg = event.getOption("message");
        if(msg != null){
            return new Snipe(new SnipePlayer(event.getUser()), new SnipePlayer(mA), attachment.getUrl(), now(), new SnipeGuild(event.getGuild()),msg.getAsString());
        }

        return new Snipe(new SnipePlayer(event.getUser()), new SnipePlayer(mA), attachment.getUrl(), now(), new SnipeGuild(event.getGuild()));

    }

    /*
    Add or update the snipe in the DB
     */
    public boolean process(){
        // Strategy: If id is 0, try to insert as a new snipe. If not, update.
        if(this.id == 0)
            return Database.getInstance().executeUpdate("INSERT INTO `snipes` (`userid`, `snipedid`, `attachment`, `datetime`, `message`, `guildid`) VALUES (?, ?, ?, ?, ?, ?)", sniper.getDiscordId(), sniped.getDiscordId(), getAttachUrl(), convertDate(getTime()), getMessage(), getGuild().getId());
        return Database.getInstance().executeUpdate("UPDATE snipes SET userid = ?, snipedid = ?, attachment = ?, datetime = ?, message = ?, guildid = ? WHERE id = ?", sniper.getDiscordId(), sniped.getDiscordId(), getAttachUrl(), convertDate(getTime()), getId(), getMessage(), getGuild().getId());
    }

    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

    public static Date now() {
        Calendar cal = Calendar.getInstance();
        return cal.getTime();
    }

    public static String convertDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(date.getTime());
    }

    public static Date stringToDate(String str){
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_NOW);
        try {
            return format.parse(str);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


}
