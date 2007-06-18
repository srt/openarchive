<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>

<html>
<head>
    <title>
        <fmt:message key="archive.search.title"/>
    </title>
    <meta name="pageID" content="openarchive-search"/>
    <script type="text/javascript" src="dwr/interface/AjaxFacade.js"></script>
    <script type="text/javascript" src="dwr/engine.js"></script>
    <script type="text/javascript" src="dwr/util.js"></script>

    <script type="text/javascript" src="scripts/script.aculo.us/prototype.js"></script>
    <script type="text/javascript" src="scripts/script.aculo.us/effects.js"></script>
    <script type="text/javascript" src="scripts/script.aculo.us/controls.js"></script>
    <script type="text/javascript" src="scripts/autocomplete.js"></script>

    <script type="text/javascript">

    // the callback for the auto completer
    function populateParticipant(autocompleter, token)
    {
        AjaxFacade.suggestParticipant(token, function(suggestions)
        {
            autocompleter.setChoices(suggestions)
        });
    }

    function createAutoCompleter()
    {
        new Autocompleter.DWR("p1", "p1_suggestions", populateParticipant, {})
        new Autocompleter.DWR("p2", "p2_suggestions", populateParticipant, {})
    }

    dwr.util.setEscapeHtml(false)

    function findConversations()
    {
        var participants = [ dwr.util.getValue('p1'), dwr.util.getValue('p2') ]
        var startDate = dwr.util.getValue('startDate')
        var endDate = dwr.util.getValue('endDate')
        var dateRange = dwr.util.getValue('dateRange')
        var keywords = dwr.util.getValue('keywords')

        selectedConversation = null;
        $('conversationLog').style.display = 'none';
        $('conversationLogBlank').style.display = '';
        dwr.util.removeAllRows('conversationTableBody')

        AjaxFacade.findConversations(participants, startDate, endDate, dateRange, keywords, findConversationsCB)
    }

    var selectedConversation;
    var cellFuncs = [
            function(data)
            {
                return data.ownerWith
            },
            function(data)
            {
                return data.shortDate
            },
            function(data)
            {
                return data.duration
            }
            ];

    function findConversationsCB(data)
    {
        dwr.util.addRows('conversationTableBody', data, cellFuncs, {
            rowCreator:function(options)
            {
                var tr = document.createElement("tr")
                var conversationId = options.rowData.conversationId
                tr.id = 'conversation-' + conversationId;
                tr.className = 'row' + (options.rowNum % 2);
                tr.onmouseover = function()
                {
                    if (selectedConversation != null && this.id == 'conversation-' + selectedConversation.conversationId)
                    {
                        return;
                    }
                    this.style.backgroundColor = '#ffffee';
                }
                tr.onmouseout = function()
                {
                    if (selectedConversation != null && this.id == 'conversation-' + selectedConversation.conversationId)
                    {
                        return;
                    }
                    this.style.backgroundColor = '';
                }
                tr.onclick = function()
                {
                    showConversation(conversationId)
                }
                return tr;
            },
            cellCreator:function(options)
            {
                var td = document.createElement("td")
                switch (options.cellNum)
                        {
                    case 0: td.className = "participants"; break;
                    case 1: td.className = "date"; break;
                    case 2: td.className = "duration";
                }
                return td;
            }
        })
        $('result').style.display = '';
    }

    function showConversationCB(data)
    {
        if (selectedConversation != null)
        {
            $("conversation-" + selectedConversation.conversationId).style.backgroundColor = '';
        }
        selectedConversation = data;
        $('conversation-' + data.conversationId).style.backgroundColor = '#eaf1f8';
        $('conversationLogBlank').style.display = 'none';
        $('conversationLog').style.display = '';
        dwr.util.setValue('conversation-participants', data.participantsSingleLine);
        dwr.util.setValue('conversation-date', data.date);
        dwr.util.setValue('conversation-duration', data.duration);
        dwr.util.setValue('conversationBody', data.body);
    }

    function showConversation(conversationId)
    {
        AjaxFacade.getConversation(conversationId, showConversationCB)
    }

    function enableDateFields()
    {
        //$('startDate').disabled = false;
        //$('endDate').disabled = false;
        dwr.util.setValue('dateRange', '');
        return true;
    }

    function disableDateFields()
    {
        //$('startDate').disabled = true;
        //$('endDate').disabled = true;
        dwr.util.setValue('startDate', '');
        dwr.util.setValue('endDate', '');
        return true;
    }

    </script>
    <style type="text/css">@import url( /js/jscalendar/calendar-win2k-cold-1.css );</style>
    <script type="text/javascript" src="/js/jscalendar/calendar.js"></script>
    <script type="text/javascript" src="/js/jscalendar/i18n.jsp"></script>
    <script type="text/javascript" src="/js/jscalendar/calendar-setup.js"></script>
    <link rel="stylesheet" type="text/css" href="styles/main.css"/>
</head>
<body>

<form action="archive-search.jsp" method="post">
    <div class="jive-contentBox">
        <table width="100%">
            <tr valign="top">
                <td>
                    <table>
                        <tr>
                            <td colspan="2"><b>
                                <fmt:message key="archive.search.participants"/>
                            </b></td>
                        </tr>
                        <tr valign="top">
                            <td>
                                <input type="text" id="p1" name="p1" size="30" value=""/>
                                <div id="p1_suggestions" class="auto_complete"></div>
                            </td>
                            <td><img src="images/magnifier.png" alt="" vspace="3" id="p1Trigger"/></td>
                        </tr>
                        <tr valign="top">
                            <td>
                                <input type="text" id="p2" name="p2" size="30" value=""/>
                                <div id="p2_suggestions" class="auto_complete"></div>
                            </td>
                            <td><img src="images/magnifier.png" alt="" vspace="3" id="p2Trigger"/></td>
                        </tr>
                    </table>
                </td>
                <td>
                    <table>
                        <tr>
                            <td colspan="4"><b>
                                <fmt:message key="archive.search.dateRange"/>
                            </b></td>
                        </tr>
                        <tr valign="top">
                            <td>
                                <fmt:message key="archive.search.start"/>
                            </td>
                            <td>
                                <input type="text" id="startDate" name="startDate" size="10" value="" onfocus="return enableDateFields()"/><br/>
                                <span class="jive-description"><fmt:message
                                        key="archive.search.start.description"/></span>
                            </td>
                            <td><img src="images/calendar.png" alt="" vspace="3" id="startDateTrigger"/></td>
                            <td rowspan="2">
                                <input type="radio" name="dateRange" value="lastDay" id="lastDay" checked="checked" onclick="return disableDateFields()"/>
                                <label for="lastDay"><fmt:message key="archive.search.lastDay"/></label>
                                <br/>
                                <input type="radio" name="dateRange" value="lastWeek" id="lastWeek" onclick="return disableDateFields()"/>
                                <label for="lastWeek"><fmt:message key="archive.search.lastWeek"/></label>
                                <br/>
                                <input type="radio" name="dateRange" value="lastMonth" id="lastMonth" onclick="return disableDateFields()"/>
                                <label for="lastMonth"><fmt:message key="archive.search.lastMonth"/></label>
                                <br/>
                                <input type="radio" name="dateRange" value="lastYear" id="lastYear" onclick="return disableDateFields()"/>
                                <label for="lastYear"><fmt:message key="archive.search.lastYear"/></label>
                            </td>
                        </tr>
                        <tr valign="top">
                            <td>
                                <fmt:message key="archive.search.end"/>
                            </td>
                            <td>
                                <input type="text" id="endDate" name="endDate" size="10" value="" onfocus="return enableDateFields()"/><br/>
                                <span class="jive-description"><fmt:message
                                        key="archive.search.end.description"/></span>
                            </td>
                            <td><img src="images/calendar.png" alt="" vspace="3" id="endDateTrigger"/></td>
                        </tr>
                    </table>
                </td>
                <td>
                    <table>
                        <tr>
                            <td colspan="1"><b>
                                <fmt:message key="archive.search.keywords"/>
                            </b></td>
                        </tr>
                        <tr valign="top">
                            <td>
                                <input type="text" id="keywords" name="keywords" size="30" value=""/><br/>
                                <span class="jive-description"><fmt:message
                                        key="archive.search.keywords.description"/></span>
                            </td>
                        </tr>
                        <tr valign="top">
                            <td align="right">
                                <br/>
                                <input type="submit" name="search" value="<fmt:message key="archive.search.submit"/>"
                                       onclick="findConversations(); return false"/>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
    </div>
</form>

<table id="result" style="display:none;">
    <tr>
        <td width="35%">
            <div id="conversationList">
                <table id="conversationTable">
                    <tbody id="conversationTableBody"></tbody>
                </table>
            </div>
        </td>
        <td width="60%">
            <div id="conversationLogBlank">
                <br/>
                <p style="text-align:center;"><fmt:message key="archive.search.selectConversation"/></p>
            </div>
            <div id="conversationLog" style="display:none;">
                <div id="conversationHeader">
                    <span class="small-label"><fmt:message key="archive.search.participants"/>:</span>
                    <span class="small-text" id="conversation-participants"></span><br/>
                    <span class="small-label"><fmt:message key="archive.search.date"/>:</span>
                    <span class="small-text" id="conversation-date"></span><br/>
                    <span class="small-label"><fmt:message key="archive.search.duration"/>:</span>
                    <span class="small-text" id="conversation-duration"></span>
                </div>
                <div id="conversationBody">
                    
                </div>
            </div>

        </td>
    </tr>
</table>


<script type="text/javascript">

    function checkDateRange()
    {
        var endDateField = $('endDate')
        var startDateField = $('startDate')

        var endDate = new Date(endDateField.value)
        var startDate = new Date(startDateField.value)
        if (endDate.getTime() < startDate.getTime())
        {
            alert("<fmt:message key="archive.search.error.endBeforeStart"/>")
            startDateField.value = ""
        }
        else
        {
             enableDateFields()
        }
    }

    Calendar.setup(
    {
        inputField  : "startDate",
        ifFormat    : "%m/%d/%y",
        button      : "startDateTrigger",
        onUpdate    : checkDateRange
    })

    Calendar.setup(
    {
        inputField  : "endDate",
        ifFormat    : "%m/%d/%y",
        button      : "endDateTrigger",
        onUpdate    : checkDateRange
    })

    createAutoCompleter()

</script>


</body>
</html>