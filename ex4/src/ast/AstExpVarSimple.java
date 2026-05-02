package ast;

import ir.Ir;
import ir.IrCommandLoad;
import temp.Temp;
import temp.TempFactory;
import types.*;
import symboltable.*;

public class AstExpVarSimple extends AstExpVar {
    /************************/
    /* simple variable name */
    /************************/
    public String name;
    public int line;

    /******************/
    /* CONSTRUCTOR(S) */

    /******************/
    public AstExpVarSimple(String name, int line) {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        System.out.format("====================== var -> ID( %s )\n", name);
        this.name = name;
        this.line = line;
    }

    /**************************************************/
    /* The printing message for a simple var AST node */

    /**************************************************/
    public void printMe() {
        /**********************************/
        /* AST NODE TYPE = AST SIMPLE VAR */
        /**********************************/
        System.out.format("AST NODE SIMPLE VAR( %s )\n", name);

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("SIMPLE\nVAR\n(%s)", name));
    }

    public Type semantMe() {
        Type t = null;
        TypeClass currentClass = SymbolTable.getInstance().getCurrentClass();
        if (currentClass != null) {
            t = currentClass.findInDataMembers(name);
        }
        if (t == null) {
            t = SymbolTable.getInstance().find(name);
        }

        if (t == null) {
            System.out.format(">> ERROR [%d:%d] variable %s not found\n", line, line, name);
            throw new Error("ERROR(" + line + ")");
        }
        return t;
    }

    public Temp irMe()
	{
		Temp t = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IrCommandLoad(t,name));
		return t;
	}
}
