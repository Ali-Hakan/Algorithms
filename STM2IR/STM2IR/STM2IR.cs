using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace STM2IR
{
    /// <summary>
    /// This is the top class of the specified program.
    /// </summary>
    internal class STM2IR
    {
        static readonly string textFile = @"C:\Users\hadda\Downloads\STM.txt";
        private static SortedSet<string> variables = new SortedSet<string>();
        private static LinkedList<string> IRCode = new LinkedList<string>();
        private static int lineCounter = 0;
        private static int tempCounter = 0;

        /// <summary>
        /// This program converts input STM code to IR code.
        /// </summary>
        /// <param name="args"></param>
        static void Main(string[] args)
        {

            IRCode.AddLast("; ModuleID = 'stm2ir'");
            IRCode.AddLast("declare i32 @printf(i8*, ...)");
            IRCode.AddLast("@print.str = constant [4 x i8] c\"%d\\0A\\00\"");
            IRCode.AddLast("define i32 @main() {");

            if (File.Exists(textFile))
            {
                using (StreamReader file = new StreamReader(textFile))
                {
                    string line;
                    while ((line = file.ReadLine()) != null)
                    {
                        lineCounter++;
                        if (line.Contains('='))
                        {
                            string variablePart = line.Split("=")[0], expressionPart = line.Split("=")[1];
                            if (!variables.Contains(variablePart))
                            {
                                variables.Add(variablePart);
                                IRCode.AddLast("%" + variablePart + " = alloca i32");
                            }
                            IRCode.AddLast("store i32 " + FindVariable(HandleExpression(expressionPart)) + ", i32* %" + variablePart);
                        }
                        else
                        {
                            IRCode.AddLast("call i32 (i8*, ...)* @printf(i8* getelementptr ([4 x i8]* @print.str, i32 0, i32 0), i32 " + FindVariable(HandleExpression(line)) + " )");
                            tempCounter++;
                        }
                    }
                    file.Close();
                }
            }
            IRCode.AddLast("ret i32 0");
            IRCode.AddLast("}");
            FileStream fileStream = new FileStream(@"C:\Users\hadda\Downloads\LLVMIR.txt", FileMode.Create);
            StreamWriter streamWriter = new StreamWriter(fileStream);
            foreach (var elements in IRCode)
                streamWriter.Write(elements + "\n");            
            streamWriter.Close();
            fileStream.Close();
        }

        /// <summary>
        /// According to operator priority, expressions are distinguished by their operators.
        /// </summary>
        /// <param name="expression"></param>
        /// <returns>expression</returns>
        static string HandleExpression(string expression)
        {
            while (expression.Contains("(") || expression.Contains(")"))
            {
                int begin = expression.LastIndexOf('('), end = expression.IndexOf(')', begin);
                string inside = expression.Substring(begin + 1, end - (begin + 1));
                expression = expression.Replace("(" + inside + ")", HandleExpression(inside));
            }

            expression = OperateExpression(expression, "*", "mul");
            expression = OperateExpression(expression, "/", "sdiv");
            expression = OperateExpression(expression, "-", "sub");
            expression = OperateExpression(expression, "+", "add");
            return expression;
        }

        /// <summary>
        /// Variables must be dissociated from static values, so put into a custom list.
        /// </summary>
        /// <param name="expression"></param>
        /// <returns>expression</returns>
        static string FindVariable(string expression)
        {
            try
            {
                Int32.Parse(expression);
            }
            catch (Exception)
            {
                if (variables.Contains(expression))
                {
                    if (expression[0] != '%')
                    {
                        string originalExpression = expression;
                        expression = "%" + ++tempCounter;
                        variables.Add(expression);
                        IRCode.AddLast(expression + " = load i32* %" + originalExpression);
                    }
                }
            }
            return expression;
        }

        /// <summary>
        /// After expression is decomposed into its variables, static values and operators, mathematical operations must be handled.
        /// </summary>
        /// <param name="expression"></param>
        /// <param name="operators"></param>
        /// <param name="type"></param>
        /// <returns>expression</returns>
        static string OperateExpression(string expression, string operators, string type)
        {
            while (expression.Contains(operators))
            {
                int leftEnd = expression.IndexOf(operators) - 1, leftBegin = leftEnd, rightBegin = expression.IndexOf(operators) + 1, rightEnd = rightBegin;
                while (true)
                {
                    if (leftBegin - 1 < 0)
                    {
                        break;
                    }
                    if (expression[leftBegin - 1] == '+' || expression[leftBegin - 1] == '-' || expression[leftBegin - 1] == '*' || expression[leftBegin - 1] == '/')
                    {
                        break;
                    }
                    leftBegin--;
                }
                while (true)
                {
                    if (rightEnd + 1 >= expression.Length)
                    {
                        break;
                    }
                    if (expression[rightEnd + 1] == '+' || expression[rightEnd + 1] == '-' || expression[rightEnd + 1] == '*' || expression[rightEnd + 1] == '/')
                    {
                        break;
                    }
                    rightEnd++;
                }
                string left = expression.Substring(leftBegin, (leftEnd + 1) - leftBegin), right = expression.Substring(rightBegin, (rightEnd + 1) - rightBegin);
                string tempLeft = FindVariable(left), tempRight = FindVariable(right), temp = "%" + ++tempCounter;
                variables.Add(temp);
                IRCode.AddLast(temp + " = " + type + " i32 " + tempLeft + "," + tempRight);
                expression = expression.Replace(left + operators + right, temp);
            }
            return expression;
        }
    }
}
