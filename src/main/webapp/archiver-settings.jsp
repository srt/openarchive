<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>
<%@ page import="org.jivesoftware.util.ParamUtils" %>
<%@ page import="com.reucon.openfire.plugin.archiver.ArchiverPlugin" %>

<% // Get parameters
    boolean update = request.getParameter("update") != null;
    boolean updateSuccess = false;
    boolean componentEnabled = ParamUtils.getBooleanParameter(request, "componentEnabled");
    Integer conversationTimeout = ParamUtils.getIntParameter(request, "conversationTimeout", -1);

    ArchiverPlugin plugin = ArchiverPlugin.getInstance();

    // Perform update if requested
    if (update)
    {
        if (componentEnabled != plugin.isEnabled())
        {
            plugin.setEnabled(componentEnabled);
            updateSuccess = true;
        }
        if (conversationTimeout > 0 && conversationTimeout != plugin.getConversationTimeout())
        {
            plugin.setConversationTimeout(conversationTimeout);
            updateSuccess = true;
        }
    }

    // Populate form
    componentEnabled = plugin.isEnabled();
    conversationTimeout = plugin.getConversationTimeout();
%>

<html>
<head>
    <title>
        <fmt:message key="archiver.settings.title"/>
    </title>
    <meta name="pageID" content="archiver-settings"/>
    <script src="dwr/engine.js" type="text/javascript"></script>
    <script src="dwr/util.js" type="text/javascript"></script>
    <script src="dwr/interface/AjaxFacade.js" type="text/javascript"></script>
</head>
<body>

<p>
    <fmt:message key="archiver.settings.intro"/>
</p>

<% if (updateSuccess)
{ %>

<div id="updateSuccessMessage" class="success">
   <fmt:message key="archiver.settings.update.success"/>
</div>
<script type="text/javascript">
    setTimeout("Effect.Fade('updateSuccessMessage')", 3000)
</script>

<% } %>

<div id="rebuildIndexSuccessMessage" class="success" style="display:none;">
    <fmt:message key="archiver.settings.rebuildIndex.success">
        <fmt:param><span id="rebuildIndexNumberOfMessages"></span></fmt:param>
    </fmt:message>
</div>

<div id="rebuildIndexErrorMessage" class="error" style="display:none;">
    <fmt:message key="archiver.settings.rebuildIndex.error"/>
</div>

<form action="archiver-settings.jsp" method="post">
    <div class="jive-contentBoxHeader">
        <fmt:message key="archiver.settings.basic.title"/>
    </div>
    <div class="jive-contentBox">
        <table cellpadding="3" cellspacing="0" border="0">
            <tbody>
                <tr>
                    <td width="1%" valign="top" nowrap>
                        <input type="radio" name="componentEnabled" value="false" id="rb01"
                        <%= (!componentEnabled ? "checked" : "") %>>
                    </td>
                    <td width="99%">
                        <label for="rb01">
                            <fmt:message key="archiver.settings.basic.disabled"/>
                        </label>
                    </td>
                </tr>
                <tr>
                    <td width="1%" valign="top" nowrap>
                        <input type="radio" name="componentEnabled" value="true" id="rb02"
                        <%= (componentEnabled ? "checked" : "") %>>
                    </td>
                    <td width="99%">
                        <label for="rb02">
                            <fmt:message key="archiver.settings.basic.enabled"/>
                        </label>
                    </td>
                </tr>
                <tr>
                    <td width="1%" nowrap>&nbsp;</td>
                    <td width="99%">
                        <fmt:message key="archiver.settings.basic.conversationTimeout">
                            <fmt:param><input type="text" name="conversationTimeout" size="3" maxlength="6"
                                              value="<%= conversationTimeout %>"/></fmt:param>
                        </fmt:message>
                    </td>
                </tr>
                <tr>
                    <td width="1%" nowrap>&nbsp;</td>
                    <td width="99%">
                        <fmt:message key="archiver.settings.rebuildIndex.intro"/>
                        <span id="rebuildIndex"><a href="javascript:rebuildIndex()">
                            <fmt:message key="archiver.settings.rebuildIndex"/>
                        </a></span>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
    <input type="submit" name="update" value="<fmt:message key="archiver.settings.save"/>"/>
</form>

<script type="text/javascript">
    function rebuildIndex()
    {
        $("rebuildIndex").innerHTML = '<img src="/images/working-16x16.gif" border="0"/>'
        AjaxFacade.rebuildIndex(rebuildIndexCB)
    }

    function rebuildIndexCB(data)                 
    {
        $("rebuildIndex").innerHTML = '<a href="javascript:rebuildIndex()"><fmt:message key="archiver.settings.rebuildIndex" /></a>'
        if (data == -1)
        {
            $("rebuildIndexErrorMessage").style.display = ''
            setTimeout("Effect.Fade('rebuildIndexErrorMessage')", 5000)
        }
        else
        {
            $("rebuildIndexSuccessMessage").style.display = ''
            $("rebuildIndexNumberOfMessages").innerHTML = data
            setTimeout("Effect.Fade('rebuildIndexSuccessMessage')", 3000)
        }
    }
</script>

</body>
</html>