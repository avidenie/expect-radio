package ro.expectations.radio.service

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.XmlResourceParser
import android.os.Process
import android.util.Base64
import org.xmlpull.v1.XmlPullParserException
import ro.expectations.radio.R
import ro.expectations.radio.utilities.Logger
import java.io.IOException
import java.util.ArrayList
import java.util.HashMap

class PackageValidator(context: Context) {

    companion object {
        private const val TAG = "PackageValidator"
    }

    private val validCertificates: Map<String, ArrayList<CallerInfo>> = readValidCertificates(
            context.resources.getXml(R.xml.allowed_media_browser_callers))

    private fun readValidCertificates(parser: XmlResourceParser): Map<String, ArrayList<CallerInfo>> {
        val validCertificates = HashMap<String, ArrayList<CallerInfo>>()
        try {
            var eventType = parser.next()
            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG && parser.name == "signing_certificate") {

                    val name = parser.getAttributeValue(null, "name")
                    val packageName = parser.getAttributeValue(null, "package")
                    val isRelease = parser.getAttributeBooleanValue(null, "release", false)
                    val certificate = parser.nextText().replace("\\s|\\n".toRegex(), "")

                    val info = CallerInfo(name, packageName, isRelease)

                    var infos: ArrayList<CallerInfo>? = validCertificates[certificate]
                    if (infos == null) {
                        infos = ArrayList()
                        validCertificates[certificate] = infos
                    }
                    Logger.v(TAG, "Adding allowed caller: ", info.name,
                            " package=", info.packageName, " release=", info.release,
                            " certificate=", certificate)
                    infos.add(info)
                }
                eventType = parser.next()
            }
        } catch (e: XmlPullParserException) {
            Logger.e(TAG, e, "Could not read allowed callers from XML.")
        } catch (e: IOException) {
            Logger.e(TAG, e, "Could not read allowed callers from XML.")
        }

        return validCertificates
    }

    fun isCallerAllowed(context: Context, callingPackage: String, callingUid: Int): Boolean {

        // Always allow calls from the framework, self app or development environment.
        if (Process.SYSTEM_UID == callingUid || Process.myUid() == callingUid) {
            return true
        }

        if (isPlatformSigned(context, callingPackage)) {
            return true
        }

        val packageInfo = getPackageInfo(context, callingPackage) ?: return false
        if (packageInfo.signatures.size != 1) {
            Logger.w(TAG, "Caller does not have exactly one signature certificate!")
            return false
        }
        val signature = Base64.encodeToString(
                packageInfo.signatures[0].toByteArray(), Base64.NO_WRAP)

        // Test for known signatures:
        val validCallers = validCertificates[signature]
        if (validCallers == null) {
            Logger.v(TAG, "Signature for caller ", callingPackage, " is not valid: \n", signature)
            if (validCertificates.isEmpty()) {
                Logger.w(TAG, "The list of valid certificates is empty. Either your file ",
                        "res/xml/allowed_media_browser_callers.xml is empty or there was an error ",
                        "while reading it. Check previous log messages.")
            }
            return false
        }

        // Check if the package name is valid for the certificate:
        val expectedPackages = StringBuffer()
        for (info in validCallers) {
            if (callingPackage == info.packageName) {
                Logger.v(TAG, "Valid caller: ", info.name, "  package=", info.packageName,
                        " release=", info.release)
                return true
            }
            expectedPackages.append(info.packageName).append(' ')
        }

        Logger.i(TAG, "Caller has a valid certificate, but its package doesn't match any ",
                "expected package for the given certificate. Caller's package is ", callingPackage,
                ". Expected packages as defined in res/xml/allowed_media_browser_callers.xml are (",
                expectedPackages, "). This caller's certificate is: \n", signature)

        return false
    }

    private fun isPlatformSigned(context: Context, pkgName: String): Boolean {
        val platformPackageInfo = getPackageInfo(context, "android")

        // Should never happen.
        if (platformPackageInfo?.signatures == null
                || platformPackageInfo.signatures.isEmpty()) {
            return false
        }

        val clientPackageInfo = getPackageInfo(context, pkgName)

        return (clientPackageInfo?.signatures != null
                && clientPackageInfo.signatures.isNotEmpty() &&
                platformPackageInfo.signatures[0] == clientPackageInfo.signatures[0])
    }

    private fun getPackageInfo(context: Context, pkgName: String): PackageInfo? {
        try {
            val pm = context.packageManager
            return pm.getPackageInfo(pkgName, PackageManager.GET_SIGNATURES)
        } catch (e: PackageManager.NameNotFoundException) {
            Logger.w(TAG, e, "Package manager can't find package: ", pkgName)
        }

        return null
    }

    private class CallerInfo(internal val name: String, internal val packageName: String, internal val release: Boolean)
}
