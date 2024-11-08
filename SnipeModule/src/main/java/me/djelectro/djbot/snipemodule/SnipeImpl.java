package me.djelectro.djbot.snipemodule;

import me.djelectro.djbot.Database;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SnipeImpl {

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


    public SnipeImpl(SnipePlayer sniper, SnipePlayer sniped, String attachUrl, Date time, SnipeGuild g){
        this.sniped = sniped;
        this.sniper = sniper;
        this.attachUrl = attachUrl;
        this.time = time;
        this.guild = g;
    }

    public SnipeImpl(SnipePlayer sniper, SnipePlayer sniped, String attachUrl, Date time, SnipeGuild g, String message){
        this.sniped = sniped;
        this.sniper = sniper;
        this.attachUrl = attachUrl;
        this.time = time;
        this.message = message;
        this.guild = g;
    }

    // If we only have the ID, we should try to retrieve the rest of the information from the database
    public SnipeImpl(int id, Guild g){
        String[] row = Database.getInstance().executeAndReturnData("SELECT * FROM snipes WHERE id = ?", id).entrySet().iterator().next().getValue();
        this.sniper = new SnipePlayer(g.getMemberById(row[1]));
        this.sniped = new SnipePlayer(g.getMemberById(row[2]));
        this.attachUrl = row[3];
        this.time = stringToDate(row[4]);
        this.message = row[5];
    }

    public int getId(){return id;}

    public static SnipeImpl createSnipe(CommandInteractionPayload event){
        try {
            OptionMapping m = event.getOption("member");
            if (m == null) {
                return null;
            }
            Member mA = m.getAsMember();

            OptionMapping oMI = event.getOption("attachment");
            if (oMI == null) {
                return null;
            }
            Message.Attachment attachment = oMI.getAsAttachment();

            OptionMapping msg = event.getOption("message");
            if (msg != null) {
                return new SnipeImpl(new SnipePlayer(event.getMember()), new SnipePlayer(mA), attachment.getProxyUrl(), now(), new SnipeGuild(event.getGuild()), msg.getAsString());
            }

            return new SnipeImpl(new SnipePlayer(event.getMember()), new SnipePlayer(mA), attachment.getProxyUrl(), now(), new SnipeGuild(event.getGuild()));
        }catch(IllegalStateException _){
            return null;
        }

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
