<xsl:stylesheet	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!--
	Rewrite image refs using {$imagedir} param
-->
<xsl:template match="img">
	<xsl:choose>
	<xsl:when test="$imagedir">
		<img src="{$imagedir}/{@src}"/>
	</xsl:when>
	<xsl:otherwise>
		<xsl:copy/>
	</xsl:otherwise>
	</xsl:choose>
</xsl:template>

</xsl:stylesheet>
