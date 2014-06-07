package org.ethereum.core;

import java.math.BigInteger;

import org.ethereum.net.message.BlocksMessage;
import org.ethereum.net.message.StaticMessages;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;
import org.ethereum.core.Block;
import org.ethereum.core.Genesis;
import org.ethereum.util.RLP;
import org.junit.Test;

import static org.junit.Assert.*;

public class BlockTest {
	
	// https://ethereum.etherpad.mozilla.org/12
	private String CPP_PoC5_GENESIS_HEX_RLP_ENCODED = "f8abf8a7a00000000000000000000000000000000000000000000000000000000000000000a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a023b503734ff34ddb7bd5e478f1645680ec778ab3f90007cb1c854653693e5adc80834000008080830f4240808080a004994f67dc55b09e814ab7ffc8df3686b4afb2bb53e60eae97ef043fe03fb829c0c0";
	private String CPP_PoC5_GENESIS_HEX_HASH = Hex.toHexString(StaticMessages.GENESIS_HASH);

    @Test /* got from go guy */
    public void testGenesisFromRLP(){
    	// from RLP encoding
    	byte[] genesisBytes = Hex.decode(CPP_PoC5_GENESIS_HEX_RLP_ENCODED);
    	Block genesisFromRLP = new Block(genesisBytes);
    	Block genesis = Genesis.getInstance();
    	assertEquals(Hex.toHexString(genesis.getHash()), Hex.toHexString(genesisFromRLP.getHash()));
    	assertEquals(Hex.toHexString(genesis.getParentHash()), Hex.toHexString(genesisFromRLP.getParentHash()));
    	assertEquals(Hex.toHexString(genesis.getStateRoot()), Hex.toHexString(genesisFromRLP.getStateRoot()));
    }
    
    @Test
    public void testGenesisFromNew() {
        /*	From: https://ethereum.etherpad.mozilla.org/11		
          	Genesis block is: 
             		( 
             			B32(0, 0, ...), 
        				B32(sha3(B())), 
        				B20(0, 0, ...), 
        				B32(stateRoot), 
        				B32(0, 0, ...), 
		    			P(2^22), 
        				P(0), 
        				P(0), 
        				P(1000000), 
        				P(0), 
        				P(0)
        				B()
        				B32(sha3(B(42)))
        			)
         */
    	Block genesis = Genesis.getInstance();
        assertEquals(CPP_PoC5_GENESIS_HEX_RLP_ENCODED, Hex.toHexString(genesis.getEncoded()));
        
        // Not really a good test because this compares Genesis.getHash() to itself
        assertEquals(CPP_PoC5_GENESIS_HEX_HASH, Hex.toHexString(genesis.getHash()));
    }
    
    @Test /* create BlockData from part of real RLP BLOCKS message */
    public void test3() {

        String blocksMsg = "F8C8F8C4A07B2536237CBF114A043B0F9B27C76F84AC160EA5B87B53E42C7E76148964D450A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D49347943854AAF203BA5F8D49B1EC221329C7AEBCF050D3A07A3BE0EE10ECE4B03097BF74AABAC628AA0FAE617377D30AB1B97376EE31F41AA01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D49347833FBFE884533F1CE880A0000000000000000000000000000000000000000000000000F3DEEA84969B6E95C0C0";

        byte[] payload = Hex.decode(blocksMsg);
        Block blockData = new Block(payload);
        System.out.println(blockData.toString());
    }

    @Test /* create BlockData from part of real RLP BLOCKS message POC-5 */
    public void test4() {

        String blocksMsg = "F8D1A0085F6A51A63D1FBA43D6E5FE166A47BED64A8B93A99012537D50F3279D4CEA52A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D4934794D8758B101609A9F2A881A017BA86CBE6B7F0581DA068472689EA736CFC6B18FCAE9BA7454BADF9C65333A0317DFEFAE1D4AFFF6F90A000000000000000000000000000000000000000000000000000000000000000008401EDF1A18222778609184E72A0008080845373B0B180A0000000000000000000000000000000000000000000000000D1C0D8BC6D744943C0C0";

        byte[] payload = Hex.decode(blocksMsg);
        Block blockData = new Block(payload);
        System.out.println(blockData.toString());
    }

    @Test /* todo: that message kills the traffic somehow need to check it good chance it's  not our fault */
    public void test5() {

        String blockMsg = "F91CC213F8D7F8D3A05A9C3A0F42EA90E45E1977D1215E64A92DB3B26F39C19081A957CF25CC4938E4A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479486B0F83E05CFBD8A695F6D4623ED330AB79F5407A03382B0B1446914CF7F0CFBD2595FCF6760637446C25705675252A6FF35B49AD3A000000000000000000000000000000000000000000000000000000000000000008360E6398201B38609184E72A0008309F91480845376CF4280A000000000000000000000000000000000000000000000000075B7C5E93C227F24C0C0F901AFF8D3A0D63157442969541A039BC16B6D9E48AF25ED124ED04F8B75C09404EBF312EBC2A00F3083A396728B8D2FE28293AC0BB7E4F129B473CB5840F916415D73A6603AD29486B0F83E05CFBD8A695F6D4623ED330AB79F5407A0709F217DF5C9EE697800FC29ABE3E4887C433850F97D7F9FB17B8905ED156E36A000000000000000000000000000000000000000000000000000000000000000008360CE068201B28609184E72A0008309FB9380845376CF4280A0000000000000000000000000000000000000000000000000894E32ED89D9A937C0F8D7F8D5A047FDB11571BDD91DB9AFDAEBDC6538F13B2B0F18BFB56E37C6F7EABD952BDC47A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479485000814DFDBCBD96DE0465675CA089D0CC711A1A0A8D82B4BA4ECDE43A19F3BDE008D99F97E15694569CDD2F0308CB7CB4EAC15C6A0C12CB7054BE29658C73D0E02F93D7C6ADCBE348B3617FF7E41692CE23D3417268360B5D98201B18609184E72A0008309FE12820313845376CF2A80A0000000000000000000000000000000000000000000000000237953ADF0651ACEF9019EF8D5A047FDB11571BDD91DB9AFDAEBDC6538F13B2B0F18BFB56E37C6F7EABD952BDC47A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479486B0F83E05CFBD8A695F6D4623ED330AB79F5407A02FF1AAE0D0A7FBF14E187946EB6B028D8F1716270DBD1F26200297F40748DEAFA077987FAF5286BE98D27BD462562D4E1A81B59B7B9A30A2D7C05AD1CFC48844038360B5D98201B18609184E72A0008309FE12820313845376CF2A80A00000000000000000000000000000000000000000000000001DCD333CA4D0CB0BF8C4F8C2F89C808609184E72A00082271094000000000000000000000000000000000000000080B7604060140260200A0F60FF60000B10630000001659006080600056600056036000576007603060003960076000F20000600060206001561CA0641A7E400D51FF7135A34F3E9D7BF16C8FB784A2ED00D844B025B80DD51D77C8A00828B02C77BA7A3AD6EB739854F0333D2DB18D756095EB997FB7BD2F920DFB82A0ED9DD9541C7962F7633646CC1D44444FC800EB78C2E16EC291FBD61368206794820313C0F8D7F8D3A020282FE2850D6FCC50A18D0A68FECF052281F01FC54EF2D806D112B44FED72A4A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479485000814DFDBCBD96DE0465675CA089D0CC711A1A092E3F9A5D62A31073A21EE0AA19F37C6D513CFDDF742241B047CDA89076BC9C3A0000000000000000000000000000000000000000000000000000000000000000083609DB28201B08609184E72A000830A009380845376CF2980A000000000000000000000000000000000000000000000000076CB5317F46E4E1EC0C0F8D7F8D3A00A41F765D5D6A09F12DE756012791C1173532A52F0727FED75B3790C1E48F62CA01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479486B0F83E05CFBD8A695F6D4623ED330AB79F5407A0703FB6288C3B9463380F2BAA9EDA70FC6260A30B06DE531DAA37BAC6BCB51F96A00000000000000000000000000000000000000000000000000000000000000000836085918201AF8609184E72A000830A031480845376CF2080A000000000000000000000000000000000000000000000000012B0608A249E985CC0C0F8D7F8D3A0F61C52941C66B1DA732DCE69B9327CC903B77B426A09C1AD7AAC7D8BC642DC3EA01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D49347942BE5CFECA826E623BE23BB36C6433E1DD8BBF75AA06C8CB0D52DF07942B9B9492783FC7B0F563EEF5DD9FF28317130A6E339DDFF41A0000000000000000000000000000000000000000000000000000000000000000083606D768201AE8609184E72A000830A059680845376CF1E80A0000000000000000000000000000000000000000000000000781E922736EE69C0C0C0F8D7F8D3A0D7A5C737A911EEB3B525569746C6F863A7C11DB69703B537606FF8C75DC31E00A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D49347942BE5CFECA826E623BE23BB36C6433E1DD8BBF75AA02E645EAB8B20125278167B643D68E45CC506BA3998909E10DB936EAE1E0BB45CA00000000000000000000000000000000000000000000000000000000000000000836055618201AD8609184E72A000830A081980845376CF1780A00000000000000000000000000000000000000000000000009F03B3443ED7CDB1C0C0F8D7F8D3A0C89E63C6CF2961B73F2D84D1F75E5EF298ECD7A7C9B27E53F5628CC066474254A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D49347942BE5CFECA826E623BE23BB36C6433E1DD8BBF75AA029147B8FACD35B2D7D852BE539D472CB735B2D92196A6EA7EC866CD7B6DD84D3A0000000000000000000000000000000000000000000000000000000000000000083603D528201AC8609184E72A000830A0A9C80845376CF0080A0000000000000000000000000000000000000000000000000F3CBD47433EC51F2C0C0F8D7F8D3A05B6CEC1B8878B85B6332725CA026DE0C35E284D5E4FFC779FECEE16FF6C86F21A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479486B0F83E05CFBD8A695F6D4623ED330AB79F5407A070D894DAF3EE7D1EA97A81F69E23E7632B4D09A158E5344AEDB97F2A2F93BCC2A00000000000000000000000000000000000000000000000000000000000000000836025498201AB8609184E72A000830A0D2080845376CEF180A0000000000000000000000000000000000000000000000000166728DB9360E580C0C0F8D7F8D3A091BEF98E5DFC6E3A65F4B482A10AF0613F947E55F736A9303F577ADCDD8B7F3CA01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479485000814DFDBCBD96DE0465675CA089D0CC711A1A0923A44EED76F1705643B5C6EA44F69EFEF9D45D8DB7527375B8C629EB05269BEA0000000000000000000000000000000000000000000000000000000000000000083600D468201AA8609184E72A000830A0FA480845376CEEC80A00000000000000000000000000000000000000000000000001AB7440167369CAFC0C0F8D7F8D3A063CE861DA91055E0821754442F65DAAE8D3CB297A6250867E0CFCC28B61B68D8A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479485000814DFDBCBD96DE0465675CA089D0CC711A1A0EE8D924E5FE7CDB99C1122AD170263C632E105C12A94CF7CDCCF37E8497C81B9A00000000000000000000000000000000000000000000000000000000000000000835FF5498201A98609184E72A000830A122980845376CED980A0000000000000000000000000000000000000000000000000EFC942DD6FF7616DC0C0F8D7F8D3A06A5E905ED31F6392BA9DCFA7890A1F52CA25DD98FBDD75F3754845B6F120BEB8A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D49347942BE5CFECA826E623BE23BB36C6433E1DD8BBF75AA0FFCA836C8F25754698505572C52403DE0B33D357487B07948FE430968762A270A00000000000000000000000000000000000000000000000000000000000000000835FDD528201A88609184E72A000830A14AF80845376CED680A00000000000000000000000000000000000000000000000002122F19CDFFFBAEBC0C0F8D7F8D3A060A93B480AB9BE58A0A6EDC95828F1579D6214D8374DD76629866DEEDD2A89C9A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479486B0F83E05CFBD8A695F6D4623ED330AB79F5407A06C419EAA73ADB5117F4D3063C0C19E2CE7E7A6BC6A39D4B310DC10068FE1B247A00000000000000000000000000000000000000000000000000000000000000000835FC5618201A78609184E72A000830A173580845376CECE80A00000000000000000000000000000000000000000000000005FEE3C618DB72604C0C0F8D7F8D3A0D5AAC64688E132C60532521DD8CAF3176D20BE18205C5054E8BD2733856ACE3DA01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479485000814DFDBCBD96DE0465675CA089D0CC711A1A04E8EAAB0B801C0AFBEF8B8544C052775E1B1294A88BC2C379CD0FAD0876E1E64A00000000000000000000000000000000000000000000000000000000000000000835FAD768201A68609184E72A000830A19BC80845376CECA80A000000000000000000000000000000000000000000000000074A7E922D5F2C424C0C0F8D7F8D3A0DE1F20341C8438C9DBAFB761225F13EB8B0A4948F7A5F70CAAA141422A181C23A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479485000814DFDBCBD96DE0465675CA089D0CC711A1A02A502AF08E85CCC2B6F25FAF8AF84602ECBEC67462E060412176A9BED03AE301A00000000000000000000000000000000000000000000000000000000000000000835F95918201A58609184E72A000830A1C4480845376CEB580A00000000000000000000000000000000000000000000000004EB1A2330EBEB25EC0C0F8D7F8D3A08576EF59DE78E1A0D71129F312089B5B9489D2C7BA860F79BD3C38F0B6181792A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479485000814DFDBCBD96DE0465675CA089D0CC711A1A0E3C122E389221F00AB2D7E184673C3C3BF067293A148EBBC2297D9955316FE4BA00000000000000000000000000000000000000000000000000000000000000000835F7DB28201A48609184E72A000830A1ECC80845376CEA280A00000000000000000000000000000000000000000000000001AB4403D3339F87CC0C0F8D7F8D3A07CE7DBFDCAA4671ABE04B9079EE4651B2855B91A1A2B478C2D6DC03AEA6E90EFA01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479485000814DFDBCBD96DE0465675CA089D0CC711A1A0A4F79A0690598043C90732468FF94C319B8255AC105E994F35721F6EC5E94720A00000000000000000000000000000000000000000000000000000000000000000835F65D98201A38609184E72A000830A215580845376CE9F80A0000000000000000000000000000000000000000000000000531B2012C60B98C4C0C0F8D7F8D3A018E99074D2DBAAB4229C47AD0D43A50FB1D509DA7E2C8355CE8EE939A70231CBA01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479485000814DFDBCBD96DE0465675CA089D0CC711A1A0E281A1C194C5E8481D6290FCBCED6900160B4B99A12C61C1D26007BB009EEF89A00000000000000000000000000000000000000000000000000000000000000000835F4E068201A28609184E72A000830A23DE80845376CE8680A000000000000000000000000000000000000000000000000034A019980E2816D8C0C0F8D7F8D3A0B8D73FA39ABAA55983BF25ABD8C437EAC1B5EFCAB296AE91F178DE82B0572CEDA01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479485000814DFDBCBD96DE0465675CA089D0CC711A1A0076BF20A295EC2BBF28EEFA6D03B29263057D4B8CB4B9E5C4526689985192344A00000000000000000000000000000000000000000000000000000000000000000835F36398201A18609184E72A000830A266880845376CE7E80A0000000000000000000000000000000000000000000000000C2A29FAA134E6CE7C0C0F8D7F8D3A0483DDC1F805A88E5167EEDFD81551C0EDCAE05465B96A12B09FF5F3ACC943A4CA01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D49347942BE5CFECA826E623BE23BB36C6433E1DD8BBF75AA0C81C8A62625CBC5903BDC4507790F5A08AFA0F533962C38919205865BB7F7012A00000000000000000000000000000000000000000000000000000000000000000835F1E728201A08609184E72A000830A28F380845376CE7280A000000000000000000000000000000000000000000000000044EC79C1ACE56782C0C0F8D7F8D3A0BCBC4648BEA672F8E97FEECFF0DDAA52BEA25C69DD5AC6778064AEBD22558AB1A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479485000814DFDBCBD96DE0465675CA089D0CC711A1A0329645D0662AFE926B69310D5AB62B5F46B468C5EA868BD8383EF01E0F4EC017A00000000000000000000000000000000000000000000000000000000000000000835F06B182019F8609184E72A000830A2B7E80845376CE6880A000000000000000000000000000000000000000000000000078E192C00B5C9A55C0C0F8D7F8D3A030367C2249B99C9B64E5A54B04D2AB889E741E6163D705A551BFBAE1CAB48656A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479486B0F83E05CFBD8A695F6D4623ED330AB79F5407A053BEFC6C748B901FD0BC1ECC266887FC8AF2562857412A2B4BC2D436C13CAA07A00000000000000000000000000000000000000000000000000000000000000000835EEEF682019E8609184E72A000830A2E0A80845376CE6780A0000000000000000000000000000000000000000000000000F72A22F3893684BEC0C0F8D7F8D3A040C61152DF8987E6384FE20D9997665305B9DBDCB10E31BF9C478FEBA9BA1555A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479485000814DFDBCBD96DE0465675CA089D0CC711A1A0C628CE6F140F1CA6BB999209B04F7C12AC75527548C23296DF278D95250B2FCAA00000000000000000000000000000000000000000000000000000000000000000835ED74182019D8609184E72A000830A309780845376CE6680A0000000000000000000000000000000000000000000000000FB7592504BA17F5FC0C0F8D7F8D3A0CD12EBD76977846C8873BE9234BB52178C94BC58A2E835368F1BB496DDDA3A65A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479486B0F83E05CFBD8A695F6D4623ED330AB79F5407A0753FDE5CC9114048064959B14CDC5283C932E5D12A44DA80E1E815A652F13CA6A00000000000000000000000000000000000000000000000000000000000000000835EBF9282019C8609184E72A000830A332480845376CE6480A0000000000000000000000000000000000000000000000000D2FE571C3183D2C5C0C0F8D7F8D3A06C5ABE47363B7BD9FA8D0EB59B47024FA67490E42235A2EAD855C1BAA19363AEA01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D49347942BE5CFECA826E623BE23BB36C6433E1DD8BBF75AA081F74A210499AFB3D5B49FC07B81D80A9D84D3CABDF0B47878838BE11DBD0FE4A00000000000000000000000000000000000000000000000000000000000000000835EA7E982019B8609184E72A000830A35B280845376CE5E80A00000000000000000000000000000000000000000000000002C2EE02B36ECEA58C0C0F8D7F8D3A00C52A67A2E9F0D67F95FFB34BEB48DF1307407C2B7EC3B325FD6228698D879C4A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479486B0F83E05CFBD8A695F6D4623ED330AB79F5407A047527C3AAFAA10FC113E965DD96C5795F5A4FC6BD7F3F19AF2CA5B5E8CBEF50AA00000000000000000000000000000000000000000000000000000000000000000835E904582019A8609184E72A000830A384180845376CE5B80A0000000000000000000000000000000000000000000000000BD4C3D2D8EAD07DCC0C0F8D7F8D3A0B72DF0564F00C843F439C383E178BD5D14C36DD28E0BA25DA4C743C188117470A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479485000814DFDBCBD96DE0465675CA089D0CC711A1A0E507344C0E52E46CC4AE7BFA7EB44F5D1694196D320CBC0A1C72F5B665CFD543A00000000000000000000000000000000000000000000000000000000000000000835E78A78201998609184E72A000830A3AD080845376CE5580A0000000000000000000000000000000000000000000000000F8DE5C5A5BAB5B93C0C0F8D7F8D3A0C7BEF299EDC81D1E93810F7F2EE200E29F292E3AE289A8FD083819BBCB0C0B17A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479485000814DFDBCBD96DE0465675CA089D0CC711A1A0B3918F7DED608484B5AF55332795D4D9B78E66AA7BD255148895556F0E888920A00000000000000000000000000000000000000000000000000000000000000000835E610F8201988609184E72A000830A3D6080845376CE4680A000000000000000000000000000000000000000000000000019A734F9E48CA7C8C0C0F8D7F8D3A0D5CB93F1E08E8A42E13BC200AA97C19189778E36BD010BCECBF513FC55267C88A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479485000814DFDBCBD96DE0465675CA089D0CC711A1A00EA85CC320772CA355D74748716EC623A381473BB34B98271F33B24D90F674BCA00000000000000000000000000000000000000000000000000000000000000000835E497D8201978609184E72A000830A3FF080845376CE4680A00000000000000000000000000000000000000000000000005A7AF036D1551C24C0C0F8D7F8D3A058BA4DEB3E947B951053B7402FF400B3663B9B0184D86A915C5383C132D71B5CA01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479485000814DFDBCBD96DE0465675CA089D0CC711A1A02A5E467771A657D6E9FF64B1AB7BB2DEFB706F207A601AFF7E9AA9E6E93B9C69A00000000000000000000000000000000000000000000000000000000000000000835E31F18201968609184E72A000830A428180845376CE4080A0000000000000000000000000000000000000000000000000B8420BC2D2543481C0C0F8D7F8D3A00F54DE3B125BAD46E29A3CE8D3F634645C662A7F5717C922310FB5B3B280D90FA01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479485000814DFDBCBD96DE0465675CA089D0CC711A1A059BBF472F4B3EC6A82D9645250AB99A45E7488EAE98B25177149854B81763F46A00000000000000000000000000000000000000000000000000000000000000000835E1A6B8201958609184E72A000830A451380845376CE3D80A00000000000000000000000000000000000000000000000002185AD9F49F845C0C0C0F8D7F8D3A0C1938EBFBEAAC1A63E92FB44C4CCDB93F35DA9BBF9CB5EC5CCC7DE05F1278DE9A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D493479485000814DFDBCBD96DE0465675CA089D0CC711A1A0B2343359124953AB815B560053FA8EEE1A75A2FB67CE5971ACA2B56C047ADCA8A00000000000000000000000000000000000000000000000000000000000000000835E02EB8201948609184E72A000830A47A580845376CE3480A0000000000000000000000000000000000000000000000000EBFC6D9834899A53C0C0";

        byte[] payload = Hex.decode(blockMsg);
        RLPList rlpList = RLP.decode2(payload);

        BlocksMessage blockData = new BlocksMessage(rlpList);
        System.out.println(blockData.toString());
    }
    
    @Test
    public void testCalcDifficulty() {
     	Block genesis = Genesis.getInstance();
      	byte[] diffBytes = genesis.calcDifficulty();
      	BigInteger difficulty = new BigInteger(1, diffBytes);
    	System.out.println("Genesis difficulty = " + difficulty.toString());
    	fail("Yet to be implemented.");
    }
    
    @Test
    public void testCalcGasLimit() {
    	Block genesis = Genesis.getInstance();
    	long gasLimit = genesis.calcGasLimit();
    	System.out.println("Genesis gasLimit = " + gasLimit);
    	fail("Yet to be implemented.");
    }
}