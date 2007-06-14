package com.reucon.openfire.plugin.archive.ajax;

import com.reucon.openfire.plugin.archive.ArchivePlugin;
import com.reucon.openfire.plugin.archive.IndexManager;
import com.reucon.openfire.plugin.archive.PersistenceManager;
import com.reucon.openfire.plugin.archive.model.Conversation;

import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class AjaxFacade
{
    private static final String LAST_DAY = "lastDay";
    private static final String LAST_WEEK = "lastWeek";
    private static final String LAST_MONTH = "lastMonth";
    private static final String LAST_YEAR = "lastYear";
    private static final String DATE_FORMAT = "MM/dd/yy";

    public AjaxFacade()
    {
    }

    public int rebuildIndex()
    {
        return getIndexManager().rebuildIndex();
    }

    public Collection<String> suggestParticipant(String token)
    {
        return getIndexManager().searchParticipant(token);
    }

    public Collection<FormattedConversation> findConversations(String[] participants, String startDateString,
                                                               String endDateString, String rangeLiteral, String keywords)
    {
        Collection<Conversation> conversations;
        Collection<FormattedConversation> formattedConversations;
        Calendar cal = Calendar.getInstance();
        Date startDate;
        Date endDate = new Date();

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (LAST_DAY.equals(rangeLiteral))
        {
            cal.roll(Calendar.DAY_OF_YEAR, -1);
            startDate = cal.getTime();
        }
        else if (LAST_WEEK.equals(rangeLiteral))
        {
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            cal.roll(Calendar.WEEK_OF_YEAR, -1);
            startDate = cal.getTime();
        }
        else if (LAST_MONTH.equals(rangeLiteral))
        {
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.roll(Calendar.MONTH, -1);
            startDate = cal.getTime();
        }
        else if (LAST_YEAR.equals(rangeLiteral))
        {
            cal.set(Calendar.DAY_OF_YEAR, 1);
            cal.roll(Calendar.YEAR, -1);
            startDate = cal.getTime();
        }
        else
        {
            startDate = parseDate(startDateString);
            endDate = parseDate(endDateString);
        }

        if (keywords == null || keywords.length() == 0)
        {
            conversations = getPersistenceManager().findConversations(participants, startDate, endDate);
        }
        else
        {
            conversations = getIndexManager().findConversations(participants, startDate, endDate, keywords);
        }

        formattedConversations = new TreeSet<FormattedConversation>();

        for (Conversation conversation : conversations)
        {
            formattedConversations.add(new FormattedConversation(conversation));
        }

        return formattedConversations;
    }

    public FormattedConversation getConversation(Long conversationId)
    {
        Conversation conversation;

        conversation = getPersistenceManager().getConversation(conversationId);
        return new FormattedConversation(conversation);
    }

    private IndexManager getIndexManager()
    {
        return ArchivePlugin.getInstance().getIndexManager();
    }

    private PersistenceManager getPersistenceManager()
    {
        return ArchivePlugin.getInstance().getPersistenceManager();
    }

    private Date parseDate(String s)
    {
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);

        if (s == null || s.length() == 0)
        {
            return null;
        }

        try
        {
            return df.parse(s);
        }
        catch (ParseException e)
        {
            return null;
        }
    }
}
