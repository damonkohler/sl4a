<xsl:stylesheet	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output omit-xml-declaration="yes"/>

<xsl:template match="@*|node()">
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="img">
	<xsl:value-of select="@src"/>
	<!-- Is there a more straightforward way to get a newline here? -->
	<xsl:text>&#10;</xsl:text>
</xsl:template>

</xsl:stylesheet>

