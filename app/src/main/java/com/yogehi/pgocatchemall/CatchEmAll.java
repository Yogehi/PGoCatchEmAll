package com.yogehi.pgocatchemall;

import java.nio.ByteBuffer;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class CatchEmAll implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        /** checks for pokemongo **/
        if (!lpparam.packageName.equals("com.nianticlabs.pokemongo"))
            return;

        Class<?> NiaNetClass = XposedHelpers.findClass("com.nianticlabs.nia.network.NiaNet", lpparam.classLoader);

        /** hook onto the specific method in PGo**/
        findAndHookMethod(NiaNetClass, "doSyncRequest", long.class, int.class, String.class, int.class, String.class, ByteBuffer.class, int.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                /** arg[0] = object long
                 arg[1] = request id integer
                 arg[2] = url string
                 arg[3] = method integer
                 arg[4] = headers string
                 arg[5] = bytebuffer / THE DATA
                 arg[6] = bytebuffer body offset int
                 arg[7] = bytebuffer body size int **/

                /** cast arg[5] into a new bytebuffer **/
                ByteBuffer buf = (ByteBuffer) param.args[5];
                byte[] rawBuf = new byte[buf.limit()];
                buf.get(rawBuf);

                /** get a hex string out of the bytebuffer **/
                String bufHex = UtilFunctions.getHexString(rawBuf);

                /** break down the bytebuffer **/
                /** the type of request; if the request type matches HEX 0867, then it is a CATCH_POKEMON request **/
                String finalHexRequestType = bufHex.substring(28, 32);
                /** length of all of the first bracket of the protobuf bracket, which should be throw information if it's a CATCH_POKEMON request **/
                String cutWorkLengthAll = bufHex.substring(26, 28);
                int intWorkLengthAll = (int) Long.parseLong(cutWorkLengthAll, 16);

                /** cut out the string that potentially has all the throw data **/
                String cutCheckString = bufHex.substring(28, 28 + intWorkLengthAll + intWorkLengthAll);

                /** if the request type is CATCH_POKEMON and the throw data has 'hit_pokemon = true', then do this: **/
                if(finalHexRequestType.contains("0867") && cutCheckString.contains("2801")) {

                    /** further breakdown of the bytebuffer **/
                    /** beginning of request **/
                    String finalHexStart = bufHex.substring(0, 26);
                    /** useless for now but might be useful later data **/
                    String finalHexJunk = bufHex.substring(32, 34);
                    /** length of the specific throw information which includes encounter id, spawn location, reticle size, curveball, hit location **/
                    String cutWorkLength = bufHex.substring(34, 36);
                    /** placeholder before encounter id **/
                    String finalHexJunk2 = bufHex.substring(36, 38);
                    /** encounter id; always 20 HEX characters long **/
                    String finalEncounterId = bufHex.substring(38, 58);
                    /** make integer out of the specific throw information **/
                    int intWorkLength = (int) Long.parseLong(cutWorkLength, 16);
                    /** finds the length of the throw information after the encounter id; isolates the throw information to spawn location, reticle size, curveball and hit location **/
                    int weirdPlaceHolder = 58 + intWorkLength + intWorkLength - 18;
                    /** cuts out the spawn location **/
                    String cutWorkString = bufHex.substring(58, weirdPlaceHolder);
                    /** cuts out the rest of the request after the throw information **/
                    String cutHexRest = bufHex.substring(weirdPlaceHolder);

                    /** replaces reticle size to super small size, makes your spin modifier to max and hit position is center **/
                    String newWorkHex1 = cutWorkString.replaceAll("19(.*?)22", "190000008094BEEE3F22");
                    String newWorkHex2 = newWorkHex1.replaceAll("2801(.*?)2202", "280131333333333333EB3F39000000000000F03F2202");

                    /** time to rebuild everything i broke down! **/
                    /** first, find the length of the specific throw information **/
                    int intNewWorkLength = finalHexJunk2.length() + finalEncounterId.length() + newWorkHex2.length();
                    /** we don't want to count '2202' as part of the throw; that's part of the next protobuf bracket **/
                    int intNewWorkLength2 = intNewWorkLength - 4;
                    /** divide this integer by 2 since we are working with a string of HEX characters; we want the byte count instead **/
                    int intFinalWorkLength = intNewWorkLength2 / 2;
                    /** convert this number to a HEX value **/
                    String tempHexWorkLength1 = Integer.toHexString(intFinalWorkLength);
                    /** make it uppercase because i'm paranoid **/
                    String finalHexWorkLength1 = tempHexWorkLength1.toUpperCase();

                    /** now combine everything including the request type **/
                    String newWorkHex3 = finalHexRequestType + finalHexJunk + finalHexWorkLength1 + finalHexJunk2 + finalEncounterId + newWorkHex2;
                    /** find the length of this monster hex string **/
                    int intTempWorkHex1 = newWorkHex3.length();
                    /** we don't want to count '2202' as part of the throw; that's part of the next protobuf bracket **/
                    int intTempWorkHex2 = intTempWorkHex1 - 4;
                    /** divide this integer by 2 since we are working with a string of HEX characters; we want the byte count instead **/
                    int intFinalHexLength = intTempWorkHex2 / 2;
                    /** convert this number to a HEX value **/
                    String tempHexLength = Integer.toHexString(intFinalHexLength);
                    /** make it uppercase because i'm paranoid **/
                    String finalHexLength = tempHexLength.toUpperCase();

                    /** combine combine COMBINE! including the length we just got and the rest of the bytebuffer **/
                    String finHex = finalHexStart + finalHexLength + newWorkHex3 + cutHexRest;
                    /** beause i like the word 'new' **/
                    String newBufHex = finHex;

                    /** find the length of the new bytebuffer **/
                    int intNewBufHexLength = newBufHex.length();
                    /** we gotta love dividing by 2 **/
                    int intNewBufHexFinal = intNewBufHexLength / 2;
                    /** convert the hex string back to a bytebuffer **/
                    byte[] newBuff = UtilFunctions.getByteFromHexStr(newBufHex);
                    ByteBuffer yayBuff = ByteBuffer.wrap(newBuff);
                    yayBuff.position(intNewBufHexFinal);

                    /** place new bytebuffer into arg[5] **/
                    param.args[5] = (ByteBuffer) yayBuff;
                    /** place new bytebuffer length into arg[7] **/
                    param.args[7] = intNewBufHexFinal;

                } else {
                    /** This is a different packet **/
                    String newBufHex = bufHex;
                    /** convert the hex string back to a bytebuffer **/
                    byte[] newBuff = UtilFunctions.getByteFromHexStr(newBufHex);
                    ByteBuffer yayBuff = ByteBuffer.wrap(newBuff);
                    /** placed new bytebuffer into arg[5] **/
                    param.args[5] = (ByteBuffer) yayBuff;

                }
            }
        });
    }
}
