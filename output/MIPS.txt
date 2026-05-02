.data
string_access_violation: .asciiz "Access Violation"
string_illegal_div_by_0: .asciiz "Illegal Division By Zero"
string_invalid_ptr_dref: .asciiz "Invalid Pointer Dereference"
.text
user_main:
.data
	global_user_main_retval: .word 721
.text
	li $t0,2
	sw $t0,global_PrintPrimes_param_start
	li $t0,100
	sw $t0,global_PrintPrimes_param_end
	subu $sp,$sp,4
	sw $ra,0($sp)
	jal PrintPrimes
	lw $ra,0($sp)
	addu $sp,$sp,4
	lw $t0,global_PrintPrimes_retval
	jr $ra
.text
PrintPrimes:
.data
	global_PrintPrimes_retval: .word 721
.text
.data
	global_PrintPrimes_param_start: .word 721
.text
.data
	global_PrintPrimes_param_end: .word 721
.text
.data
	global_p_0: .word 721
.text
	lw $t0,global_PrintPrimes_param_start
	sw $t0,global_p_0
.text
Label_1_start:
	lw $t2,global_p_0
	lw $t1,global_PrintPrimes_param_end
	li $t0,1
	add $t0,$t1,$t0
	blt $t2,$t0,Label_11_AssignOne
	bge $t2,$t0,Label_12_AssignZero
.text
Label_11_AssignOne:
	li $t0,1
	j Label_10_end
.text
Label_12_AssignZero:
	li $t0,0
	j Label_10_end
.text
Label_10_end:
	beq $t0,$zero,Label_0_end
	lw $t0,global_p_0
	sw $t0,global_IsPrime_param_p
	subu $sp,$sp,4
	sw $ra,0($sp)
	jal IsPrime
	lw $ra,0($sp)
	addu $sp,$sp,4
	lw $t0,global_IsPrime_retval
	beq $t0,$zero,Label_3_end
	lw $t0,global_p_0
	move $a0,$t0
	li $v0,1
	syscall
	li $a0,32
	li $v0,11
	syscall
.text
Label_3_end:
	lw $t1,global_p_0
	li $t0,1
	add $t0,$t1,$t0
	sw $t0,global_p_0
	j Label_1_start
.text
Label_0_end:
	jr $ra
.text
IsPrime:
.data
	global_IsPrime_retval: .word 721
.text
.data
	global_IsPrime_param_p: .word 721
.text
.data
	global_i_1: .word 721
.text
	li $t0,2
	sw $t0,global_i_1
.data
	global_j_2: .word 721
.text
	li $t0,2
	sw $t0,global_j_2
.text
Label_5_start:
	lw $t1,global_i_1
	lw $t0,global_IsPrime_param_p
	blt $t1,$t0,Label_14_AssignOne
	bge $t1,$t0,Label_15_AssignZero
.text
Label_14_AssignOne:
	li $t0,1
	j Label_13_end
.text
Label_15_AssignZero:
	li $t0,0
	j Label_13_end
.text
Label_13_end:
	beq $t0,$zero,Label_4_end
	li $t0,2
	sw $t0,global_j_2
.text
Label_7_start:
	lw $t1,global_j_2
	lw $t0,global_IsPrime_param_p
	blt $t1,$t0,Label_17_AssignOne
	bge $t1,$t0,Label_18_AssignZero
.text
Label_17_AssignOne:
	li $t0,1
	j Label_16_end
.text
Label_18_AssignZero:
	li $t0,0
	j Label_16_end
.text
Label_16_end:
	beq $t0,$zero,Label_6_end
	lw $t1,global_i_1
	lw $t0,global_j_2
	mul $t1,$t1,$t0
	lw $t0,global_IsPrime_param_p
	beq $t1,$t0,Label_20_AssignOne
	bne $t1,$t0,Label_21_AssignZero
.text
Label_20_AssignOne:
	li $t0,1
	j Label_19_end
.text
Label_21_AssignZero:
	li $t0,0
	j Label_19_end
.text
Label_19_end:
	beq $t0,$zero,Label_9_end
	li $t0,0
	sw $t0,global_IsPrime_retval
	jr $ra
.text
Label_9_end:
	lw $t1,global_j_2
	li $t0,1
	add $t0,$t1,$t0
	sw $t0,global_j_2
	j Label_7_start
.text
Label_6_end:
	lw $t1,global_i_1
	li $t0,1
	add $t0,$t1,$t0
	sw $t0,global_i_1
	j Label_5_start
.text
Label_4_end:
	li $t0,1
	sw $t0,global_IsPrime_retval
	jr $ra
	jr $ra
main:
	jal user_main
	li $v0,10
	syscall
