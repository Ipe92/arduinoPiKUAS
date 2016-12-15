using System;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.Text;
using System.IO;
//using JSON.NET;

namespace NAV_gateway
{

    //Define delegate type for callback function
    public delegate void callBack();

    class PollerCode
    {

        private Poller poller;
        private Thread pollingThread;
        private ThreadStart tThreadStart;

        /**
         * @String ipAddr = ThingWorx computer IP (v4), 
         * @int pPort = Thingworx Rest Api port,
         * @String pRestRequest = URL for rest request, example: resource/key/value ... etc
         * @callBack ErpCallBack = callback function, which sends a job request to the ERP
         * 
         * TODO make it poll thingworx once every 1-5 minute
         */
        public PollerCode(String ipAddr, int pPort, String pRestRequest)
        {
            poller = new Poller(ipAddr, pPort, pRestRequest);
            tThreadStart = new ThreadStart(poller.Caller);
            pollingThread = new Thread(tThreadStart);
            pollingThread.Start();
            //pollingThread.IsBackground = true;
        }

        /**
         * Remember to start a thread! :P
         */
        public void start()
        {
            //tThreadStart.Start();
        }

        public class Poller
        {
            private string ip;
            private int port;
            private Uri url;
            private String restRequest;
            private Boolean okLevel;
            private Boolean erpCalled;
            private callBack ErpCallBack;

            //connect to a server
            public Poller(String ipAddr, int pPort, String pRestRequest)
            {
                ip = ipAddr;
                port = pPort;
                restRequest = pRestRequest;

                url = new Uri("http://Arduino:1234@" + ip + ":" + port + "/" + restRequest);
                okLevel = false;
                erpCalled = false;
            }

            /**
             * This checks from an item level server, if
             * there is enough items. This calls the ERP
             * to do a new job to fill the item bin.
             * This doesn't do anything while the item level is too
             * low after calling the ERP.
             * */
            public void Caller()
            {
                // test if Caller() method is used
               while (true) 
               // while (Thread.CurrentThread.IsAlive)
                {
                    // poll thingworx every 10 seconds
                    // somehow doesn't work
                    Thread.Sleep(TimeSpan.FromSeconds(10));

                    String response;
                    response = ReadRestResponse(url);
                    okLevel = isLevelOk(response);

                    //This calls ERP to open new job
                    if (!erpCalled && !okLevel)
                    {
                        PurchaseOrderErp();
                        erpCalled = true;
                        // make thread sleep for 2 minutes after purchase order
                        //Thread.Sleep(TimeSpan.FromMinutes(2));
                        /*Console.WriteLine("Thread is sleeping for 2 minutes");
                        Console.Write("Time when thread is active again:");
                        DateTime sleepingTime = DateTime.Now.AddMinutes(2);
                        Console.WriteLine(sleepingTime);*/
                    }

                    //This untriggers erpCalled
                    //This is called after item level normalization
                    else if (okLevel && erpCalled)
                    {
                        erpCalled = false;
                    }




                }
            }

            private String ReadRestResponse(Uri uri)
            {
                WebClient client = new WebClient();

                //client.Credentials = new NetworkCredential("Arduino", 1234);

                //So use THIS instead to send credentials RIGHT AWAY
                string credentials = Convert.ToBase64String(
                    Encoding.ASCII.GetBytes("Arduino" + ":" + "1234"));
                client.Headers[HttpRequestHeader.Authorization] = string.Format(
                    "Basic {0}", credentials);

                client.Headers[HttpRequestHeader.Accept] = "Application/json";

                Stream data = client.OpenRead(uri);
               // client.Credentials();
                StreamReader reader = new StreamReader(data);
                string response = reader.ReadToEnd();
                data.Close();
                reader.Close();

                return response;
            }


            /**
             * This reads a response from ThingWorx
             * ands decides, if there are too few of
             * items
             */
            private Boolean isLevelOk(string response)
            {
                string[] list = response.Split(',');
                string numberVar = "";
        
                foreach (string item in list){
                    if (item.Contains("ItemAmount"))
                    {
                        numberVar = item.Split(':')[2];
                        numberVar = numberVar.Replace("}", "");
                        numberVar = numberVar.Replace(")", "");
                        numberVar = numberVar.Replace("]", "");
                    }            
                    /*
                    string[] datas = item.Split(':');

                    //datas[0] is the name
                    //datas[1] is the value
                    Console.WriteLine(datas[0]);
                    if (datas[0].Contains("ItemAmount"))
                    {
                        numberVar = datas[1].Replace(@"\", "");
                        break;
                    }*/
                }
                
                
                //dynamic data = JObject.Parse(response);
                int screwLevel = Int16.Parse(numberVar);

                if (screwLevel > 100)
                {
                    Console.WriteLine("Screw level is OK.");
                    return true;

                }
                else
                {
                    Console.WriteLine("Screw level not OK.");
                    return false;
                }
            }
        }

        private static void PurchaseOrderErp() {

            Console.WriteLine("Thingworx ItemAmount");

        }

    }
}