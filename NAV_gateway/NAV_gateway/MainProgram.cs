using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.IO;

//  http://ril-sql.rilling.local:7047/DynamicsNAV71/WS/Cronus%20Admin/Page/Job_Journal
//  USE NAVUSER3
//  The development challenge set up given in the beginning of the course was about managing the enterprise operations using
//  the SBC's, wireless XBee technology and ethernet network to trigger a job (or order) to replenish the material bin in the
//  warehouse whenever needed. The process ends up into the Dynamics NAV ERP. The Jobs part in Dynamics NAV is one possible
//  functionality for us. Using jobs you can deliver a combination of goods, services and consultation for customer.
//  Some further reading and material about NAV jobs:
//  THIS CODE ONLY WORKS FROM THE COMPUTER WHERE DYNAMIC NAV IS INSTALLED!
//  otherwise it doesn't recognize the remote name or web references
//  It uses 'ril-sql.rilling.local'
namespace NAV_gateway
{
    // We use this webreference for purchase order
    using Purchase;

    // Try to just get thingworx information without ERP

    class MainProgram

    {

        static void Main(string[] args)
        {
            // This connects to thingworx and GETs ItemAmount
            // Then if ItemAmount is too low Paramfunc gets executed
            // It creates purchase order to ERP
            //PollerCode poller = new PollerCode("172.16.28.122", 80, "/Thingworx/Things/Item A/Properties/ItemAmount/GET");
            PollerCode poller = new PollerCode("10.30.50.161", 80, "Thingworx/Things/Item A/Properties/ItemAmount/GET");
            
            //PollerCode poller = new PollerCode("127.0.0.1", 8080, "");
            poller.start();

            /*
            // This code will make a new purchase order for more screws
            // ERP callback
            Order_screws_Service screwService = new Order_screws_Service();
            Order_screws orderScrews = new Order_screws();
            // Another object for creating Purchase order line
            Purchase_Order_Line[] p = new Purchase_Order_Line[10];

            //Simple authentication and new values for Order_screws
            //screwService.UseDefaultCredentials = true;
            orderScrews.Buy_from_Vendor_No = ("1020");
            orderScrews.Buy_from_Vendor_Name = ("Cronus Cardoxy Sales");
            orderScrews.Status = Status.Released;

            // Purchase order line code
            p[0] = new Purchase_Order_Line();
            p[0].Type = Type.Item;
            p[0].TypeSpecified = true;
            p[0].No = "6666A";
            p[0].Description = ("Ruuveja");
            p[0].Quantity = (900);
            p[0].Location_Code = "ASSEMBLY";
            p[0].QuantitySpecified = true;
            p[0].Finished = true;
            // Here we assign our Order_screws to use purchase order line
            orderScrews.PurchLines = p;

            // Creates a single record. Record object is overwritten with the version that is created by the page.
            // soap exception after ~10 seconds
            //string purchase_no = "106373";
            //screwService.Delete(orderScrews.No = "purchase_no");
            //int intpurch_no = Int16.Parse(purchase_no);
            //intpurch_no++;
            //purchase_no = intpurch_no.ToString();

            // Prints some info into console (These values get into dynamic NAV)
            Console.WriteLine("New purchase order created!");
            Console.WriteLine(DateTime.Now.ToString("h:mm:ss tt"));
            Console.WriteLine("Printing purchase order line");
            Console.WriteLine("\tType: {0}", p[0].Type);
            Console.WriteLine("\tNo: {0}", p[0].No);
            Console.WriteLine("\tDescription: {0}", p[0].Description);
            Console.WriteLine("\tQuantity: {0}", p[0].Quantity);
            Console.WriteLine("\tLocation code: {0}", p[0].Location_Code);
            Console.WriteLine("\tFinished: {0}", p[0].Finished);
            */
        }

        private static callBack PurchaseOrderErp() {

            Console.WriteLine("Thingworx ItemAmount");
            return PurchaseOrderErp();
        }
    }
}