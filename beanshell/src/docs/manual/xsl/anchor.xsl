<xsl:stylesheet	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- 
	Anchor the specified text with an identifier formed by trimming space
	and translating spaces to underscores.
-->
<xsl:template name="anchor">
	<xsl:param name="value"/>
	<xsl:variable name="avalue" 
		select="translate(normalize-space($value), ' ', '_')"/>
	<a name="{$avalue}"><xsl:value-of select="$value"/></a>
</xsl:template>

<!-- 
	Link a reference to anchored text using the identifier scheme from
	the anchor template.
	
	Param: anchortofile - filename
	Param: anchorto - #ref
	Param: value - The content to link. 
	Param: defeatlink - If true disable the link.

	This template obeys $multipage to determine if the anchortofile param
	is used.
-->
<xsl:template name="anchorref">
	<xsl:param name="anchorto"/>
	<xsl:param name="anchortofile"/> <!-- default none -->
	<xsl:param name="value"/>
	<xsl:param name="defeatlink"/>

	<xsl:variable name="anchor" 
		select="translate(normalize-space($anchorto), ' ', '_')"/>
	<xsl:variable name="file" 
		select="normalize-space($anchortofile)"/>

	<xsl:choose>
	<xsl:when test="$defeatlink">
		<xsl:copy-of select="$value"/>
	</xsl:when>
	<xsl:when test="$multipage">
		<a href="{$file}#{$anchor}"><xsl:copy-of select="$value"/></a>
	</xsl:when>
	<xsl:otherwise>
		<a href="#{$anchor}"><xsl:copy-of select="$value"/></a>
	</xsl:otherwise>
	</xsl:choose>

</xsl:template>

</xsl:stylesheet>
