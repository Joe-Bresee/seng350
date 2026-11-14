module Demo
{
    sequence<string> StringSeq;

    interface BusNotificationObserver{
        void notifyCallback(string message);
    }
    interface NotificationSubject{

        void subscribe(BusNotificationObserver* callback, string busId);

        StringSeq getState();

        void sendMessage(string msg);
    }

}