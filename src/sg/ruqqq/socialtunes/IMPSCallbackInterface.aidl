package sg.ruqqq.socialtunes;
import sg.ruqqq.socialtunes.item.Song;

oneway interface IMPSCallbackInterface {
    void songChanged(in Song s);
    void playposChanged(in int ms);
}