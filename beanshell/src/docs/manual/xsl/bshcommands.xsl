<!--
	This is a stylesheet for transforming the output of bshdoc.bsh into
	an indexed HTML page of beanshell commands.
	
	This stylesheet assumes that the bshdoc output is from a set of beanshell
	commands.  The output normally shows the file comment and all method 
	signatures.  In the event that there is no file comment the comment for 
	the first method whos name matches the file name is used.  Method comments
	on other methods are not used by this stylesheet. 

	If javadoc style @method tags are supplied in the comment they will
	be used in lieu of Method sigs.
-->
<xsl:stylesheet	
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="html"/>

	<!-- Show methods in context of File element -->
	<xsl:template name="showMethods">
		<xsl:variable name="name" select="Name"/> 
		<xsl:choose>
			<!-- 
				If File Comment has method comment tags use them in lieu of
				Method elements.
			-->
			<xsl:when test="Comment/Tags/method">
				<xsl:for-each select="Comment/Tags/method">
					<xsl:value-of select="."/><br CLEAR="ALL"/>
				</xsl:for-each>
			</xsl:when>
			<xsl:when test="Method[Name=$name]/Comment/Tags/method">
				<xsl:for-each select="Method[Name=$name]/Comment/Tags/method">
					<xsl:value-of select="."/><br CLEAR="ALL"/>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<xsl:for-each select="Method">
					<xsl:value-of select="Sig"/><br CLEAR="ALL"/>
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="doIndex">
		<p/><table cellpadding="2" border="1" width="100%">
		<xsl:for-each select="File">
			<xsl:sort select="Name" order="ascending"/>
			<xsl:variable name="name" select="Name"/> 
			<tr><td width="20%"><strong><a href="#{$name}">
				<xsl:value-of select="$name"/></a></strong></td>
				<td>
					<xsl:call-template name="showMethods"/>	
				</td>
			</tr>
		</xsl:for-each>
		</table>
	</xsl:template>
	
	<xsl:template match="/">
		<html><head><title>BeanShell Command Docs</title></head>
		<body>
			<h1>BeanShell Commands</h1>
				<xsl:apply-templates/>
		</body>
		</html>
	</xsl:template>

	<xsl:template match="BshDoc">
		<xsl:call-template name="doIndex"/>	
		<p/>
		<xsl:comment>PAGE BREAK</xsl:comment>
		<xsl:apply-templates select="File">
			<xsl:sort select="Name" order="ascending"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="File">
		<xsl:variable name="name" select="Name"/> 

		<table cellpadding="5" border="0" width="100%">
		<tr><td bgcolor="#cccccc">
		<a name="{$name}">
		<strong><font size="+2">
			<xsl:value-of select="$name"/></font></strong>
		</a>
		<br CLEAR="ALL"/>
		<font size="+1">
			<xsl:call-template name="showMethods"/>	
		</font>
		</td></tr>
		<tr><td> 
			<xsl:choose>
				<xsl:when test="Comment/Text">
					<xsl:value-of 
						disable-output-escaping="yes" 
						select="Comment/Text"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of 
						disable-output-escaping="yes" 	
						select="Method[Name=$name]/Comment/Text"/>
				</xsl:otherwise>
			</xsl:choose>
		</td></tr>
		</table>
	</xsl:template>
</xsl:stylesheet>
