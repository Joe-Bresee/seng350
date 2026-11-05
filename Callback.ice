module Demo
{
    sequence<string> StringSeq;

    interface Callback{
        void notifyCallback(string message);
    }
    interface ChatManager{

        void subscribe(Callback* callback);

        StringSeq getState();

        void sendMessage(string msg);
    }

}