package tools;

import java.util.List;

public class FfmpegLocalTest {
    public static void main(String[] args) throws Exception {
        String directUrl = "https://rr4---sn-h5q7dns7.googlevideo.com/videoplayback?expire=1767249366&ei=dsFVaZ_zINbwhcIP2v2hgQU&ip=134.255.215.120&id=o-AGVTGJLZeB4oo2D3HFA8S_ZEAxjSO84otdJo-tUoWqan&itag=399&aitags=133%2C134%2C135%2C136%2C137%2C160%2C242%2C243%2C244%2C247%2C248%2C278%2C394%2C395%2C396%2C397%2C398%2C399&source=youtube&requiressl=yes&xpc=EgVo2aDSNQ%3D%3D&cps=159&met=1767227766%2C&mh=8z&mm=31%2C29&mn=sn-h5q7dns7%2Csn-h5q7knel&ms=au%2Crdu&mv=m&mvi=4&pl=24&rms=au%2Cau&gcr=es&initcwndbps=4072500&bui=AYUSA3BoQ2XJmz6QjA2W3veGDJ5w7ifFnxNgEwLEG5-CRzGdEQA3pXsOFyT94SGZq6B5jSwqNm8GIBMs&vprv=1&svpuc=1&mime=video%2Fmp4&ns=-SNnusy71LWiXCYix0boTo4R&rqh=1&gir=yes&clen=58290341&dur=230.766&lmt=1757207597345552&mt=1767227340&fvip=2&keepalive=yes&lmw=1&fexp=51557447%2C51565116%2C51565681%2C51580970&c=TVHTML5&sefc=1&txp=5537534&n=YmbTRJDOjS6acA&sparams=expire%2Cei%2Cip%2Cid%2Caitags%2Csource%2Crequiressl%2Cxpc%2Cgcr%2Cbui%2Cvprv%2Csvpuc%2Cmime%2Cns%2Crqh%2Cgir%2Cclen%2Cdur%2Clmt&lsparams=cps%2Cmet%2Cmh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Crms%2Cinitcwndbps&lsig=APaTxxMwRgIhAOTFWiFq8vgXb9IcUspX_L9R0yoeS9ayzViiCwSDouYCAiEA-U3KPM4YDQE0g0c46LmTPtGWoPgLAaV_XQvukzIRE-s%3D&sig=AJfQdSswRAIgSiwj4g5FFdpO6DATlapx2XDJgswG3I0y6daOMqvgUo8CIGn49gjUFokfCxMZ_1D-sx4fRLnjCGDpOZO6dQNjG-X1";
        String outFile = "test.mp4";

        List<String> cmd = List.of(
                "ffmpeg",
                "-y",
                "-i", directUrl,
                "-t", "10",
                "-c", "copy",
                outFile
        );

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.inheritIO();
        Process p = pb.start();
        int code = p.waitFor();
        System.out.println("Exit code: " + code);
    }
}
