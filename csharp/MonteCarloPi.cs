using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace MonteCarloPi
{
    class Program
    {
        static void Main(string[] args)
        {
            int n = Int32.Parse(args[0]);
            int concurrency = Int32.Parse(args[1]);
            int iters = n / concurrency;

            Task<int>[] tasks = new Task<int>[concurrency];
            
            for(int i = 0 ; i < concurrency ; i++)
                tasks[i] = Task.Factory.StartNew(() => {
                    Random rd = new Random();
                    return Enumerable.Range(1, iters).Select(_ =>
                    {
                        double x = rd.NextDouble();
                        double y = rd.NextDouble();

                        if (x * x + y * y < 1.0)
                            return 1;
                        else
                            return 0;
                    }).Sum();
                });

            Task.WaitAll(tasks);

            int result = tasks.Sum((t) => t.Result);
            double pi = (double)result / n * 4;

            Console.WriteLine(pi);
            Console.ReadKey(true);
        }
    }
}
