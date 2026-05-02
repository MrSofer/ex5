.data
string_access_violation: .asciiz "Access Violation"
string_illegal_div_by_0: .asciiz "Illegal Division By Zero"
string_invalid_ptr_dref: .asciiz "Invalid Pointer Dereference"
.text
user_main:
.data
	global_user_main_retval: .word 721
.text
.data
	global_y_0: .word 721
.text
	subu $sp,$sp,4
	sw $ra,0($sp)
	jal foo
	lw $ra,0($sp)
	addu $sp,$sp,4
	lw $t0,global_foo_retval
	sw $t0,global_y_0
	lw $t0,global_y_0
	move $a0,$t0
	li $v0,1
	syscall
	li $a0,32
	li $v0,11
	syscall
	jr $ra
.text
foo:
.data
	global_foo_retval: .word 721
.text
.data
	global_x_1: .word 721
.text
	li $t0,5
	sw $t0,global_x_1
	jr $ra
main:
	jal user_main
	li $v0,10
	syscall
