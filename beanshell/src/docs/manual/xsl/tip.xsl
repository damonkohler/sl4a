<xsl:stylesheet	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="tip">
	<p/>
	<center>
	<table width="100%" border="1" cellpadding="5">
	<tr><td>
	<strong>Tip:</strong>
	<br CLEAR="ALL"/>
		<xsl:apply-templates/>
	</td></tr>
	</table>
	</center>
	<p/>
</xsl:template>

</xsl:stylesheet>
