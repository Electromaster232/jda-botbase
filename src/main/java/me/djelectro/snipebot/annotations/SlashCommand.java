package me.djelectro.snipebot.annotations;

import net.dv8tion.jda.api.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SlashCommand {
    String name();

    String description();
    Permission[] perms() default Permission.ADMINISTRATOR;

    SlashCommandOption[] options() default {};
}
