#!/bin/bash
#Change config file path according to your system
source Documents/config.txt

function displayVms(){
id=1
echo "Virtual Machines currently running on this system:"
#/usr/bin/VBoxManage list vms | awk -F "\"" '{print $2}'
for i in `$VBoxMngPath/VBoxManage list runningvms | awk -F "\"" '{print $2}'` ; do
	echo $id:$i
	id=$(($id+1))
done
echo "Are you sure you want to perform a function on all the Virtual Machines?"
echo -n "Please enter yes/no: "
read ans
if [ "$ans" == "no" ] 
	then
		exit
fi		

#SelectedVM=`/usr/bin/VBoxManage list vms | awk -F "\"" '{print $2}' | sed -n "${SelectedID}p"`
}

function createTxtFiles() {
for i in `$VBoxMngPath/VBoxManage list runningvms | awk -F "\"" '{print $2}'` ; do
    if [ "$i" == "Windows" ] 
    then
        for j in {1..10}; do
			    number=$((RANDOM%3))
			    if [ "$number" == "0" ] 
			    then
			        $VBoxMngPath/VBoxManage guestcontrol $i exec --image "cmd.exe" --username $WinUsername --password $WinPassword --wait-stdout -- "/C" "echo.>$WinDir\temp\test$j.txt" 
			    fi
			    
			    if [ "$number" == "1" ] 
			    then
			        $VBoxMngPath/VBoxManage guestcontrol $i exec --image "cmd.exe" --username $WinUsername --password $WinPassword --wait-stdout -- "/C" "echo.>$WinDir\temp\sample$j.txt"
			    fi

			    if [ "$number" == "2" ] 
			    then
			        $VBoxMngPath/VBoxManage guestcontrol $i exec --image "cmd.exe" --username $WinUsername --password $WinPassword --wait-stdout -- "/C" "echo.>$WinDir\temp\trial$j.txt"
			    fi
			done


    else
        if [ "$i" == "Ubuntu" ] 
            then
                Username=$UbUsername
                Password=$UbPassword
                Path=$CommandPath4
            else
                Username=$MintUsername
                Password=$MintPassword
                Path=$CommandPath5
        fi 

        for j in {1..10}; do
				    number=$((RANDOM%3))
				    if [ "$number" == "0" ] 
				        then
				        $VBoxMngPath/VBoxManage guestcontrol $i exec --image "$CommandPath1/touch" --username $Username --password $Password --wait-stdout -- $Path/test$j.txt
				    fi
				    
				    if [ "$number" == "1" ] 
				        then
				        $VBoxMngPath/VBoxManage guestcontrol $i exec --image "$CommandPath1/touch" --username $Username --password $Password --wait-stdout -- $Path/sample$j.txt
				    fi

				    if [ "$number" == "2" ] 
				        then
				        $VBoxMngPath/VBoxManage guestcontrol $i exec --image "$CommandPath1/touch" --username $Username --password $Password --wait-stdout -- $Path/trial$j.txt
				    fi
		done                             
    fi
 done    

}

function deleteTxt(){
for i in `$VBoxMngPath/VBoxManage list runningvms | awk -F "\"" '{print $2}'` ; do
    if [ "$i" == "Windows" ] 
    then
        $VBoxMngPath/VBoxManage guestcontrol $i exec --image "cmd.exe" --username $WinUsername --password $WinPassword --wait-stdout -- "/C"  "cd $WinDir\temp & del *.txt" 
    else
        if [ "$i" == "Ubuntu" ] 
            then
                Username=$UbUsername
                Password=$UbPassword
                $VBoxMngPath/VBoxManage guestcontrol $i exec --image "$CommandPath2/rm" --username $Username --password $Password -- "-r" "$CommandPath4/"
				$VBoxMngPath/VBoxManage guestcontrol $i exec --image "$CommandPath2/mkdir" --username $Username --password $Password -- "$CommandPath4"
            else
                Username=$MintUsername
                Password=$MintPassword
                $VBoxMngPath/VBoxManage guestcontrol $i exec --image "$CommandPath2/rm" --username $Username --password $Password -- "-r" "$CommandPath5/"
				$VBoxMngPath/VBoxManage guestcontrol $i exec --image "$CommandPath2/mkdir" --username $Username --password $Password -- "$CommandPath5"
        fi                          
    fi
 done    
}


function snap(){
echo "Please enter the name of the Snapshots: "
read snapshot
for i in `$VBoxMngPath/VBoxManage list runningvms | awk -F "\"" '{print $2}'` ; do
	$VBoxMngPath/VBoxManage snapshot $i take $snapshot
done	
#$VBoxMngPath/VBoxManage snapshot Ubuntu take $snapshot
#$VBoxMngPath/VBoxManage snapshot mint take $snapshot
echo "Snapshots successfully saved.."

#/usr/bin/VBoxManage showvminfo Ubuntu | sed -n "3p" | awk -F ":" '{print $2}' | awk -F "(" '{print $1}' | awk -F " " '{print $1}'---OS
}

function resutil(){
#/usr/bin/VBoxManage showvminfo Ubuntu | sed -n "35p" | awk -F ":" '{print $2}' | awk -F " " '{print $1}'--running	
rm $ResUtilFilePath/ResourceReport.txt
for i in `$VBoxMngPath/VBoxManage list runningvms | awk -F "\"" '{print $2}'` ; do
	if [ "$i" == "Windows" ] 
	then
		echo ""| tee -a $ResUtilFilePath/ResourceReport.txt
		echo ""| tee -a $ResUtilFilePath/ResourceReport.txt
		echo "<----$i---->"| tee -a $ResUtilFilePath/ResourceReport.txt
		echo ""| tee -a $ResUtilFilePath/ResourceReport.txt
		echo ""

		#/usr/bin/VBoxManage guestcontrol Windows exec --image "cmd.exe" --username surabhi --password abcxyz --wait-stdout -- "/C"  "cd C:\Users\Surabhi & dir"
		
		echo $'<-----Key Resource Utilization Data----->\n'| tee -a $ResUtilFilePath/ResourceReport.txt 
		echo $'---CPU Utilization:---\n'| tee -a $ResUtilFilePath/ResourceReport.txt 
		$VBoxMngPath/VBoxManage guestcontrol Windows exec --image "cmd.exe" --username $WinUsername --password $WinPassword --wait-stdout -- "/C"  "cd $WinDir & cscript //nologo util.vbs" | tee -a $ResUtilFilePath/ResourceReport.txt 
		echo $'\n\n'| tee -a $ResUtilFilePath/ResourceReport.txt

		echo $'---Memory Usage:---\n'| tee -a $ResUtilFilePath/ResourceReport.txt 
		$VBoxMngPath/VBoxManage guestcontrol Windows exec --image "cmd.exe" --username $WinUsername --password $WinPassword --wait-stdout -- "/C"  "cd $WinDir & systeminfo | findstr Memory" | tee -a $ResUtilFilePath/ResourceReport.txt 
		echo $'\n\n'| tee -a $ResUtilFilePath/ResourceReport.txt 

		echo $'---Disk Usage:---\n'| tee -a $ResUtilFilePath/ResourceReport.txt 
		$VBoxMngPath/VBoxManage guestcontrol Windows exec --image "cmd.exe" --username $WinUsername --password $WinPassword --wait-stdout -- "/C"  "cd $WinDir & wmic logicaldisk get size,freespace,caption" | tee -a $ResUtilFilePath/ResourceReport.txt 
		echo $'\n\n'| tee -a $ResUtilFilePath/ResourceReport.txt 



	else
		echo ""| tee -a $ResUtilFilePath/ResourceReport.txt
		echo ""| tee -a $ResUtilFilePath/ResourceReport.txt
		echo "<----$i---->"| tee -a $ResUtilFilePath/ResourceReport.txt
		echo ""| tee -a $ResUtilFilePath/ResourceReport.txt
		if [ "$i" == "Ubuntu" ] 
			then
				Username=$UbUsername
				Password=$UbPassword
			else
				Username=$MintUsername
				Password=$MintPassword
		fi
		
		echo ""
		echo $'<-----Key Resource Utilization Data----->\n'| tee -a $ResUtilFilePath/ResourceReport.txt 
		echo $'---Memory,process and CPU related displays:---\n'| tee -a $ResUtilFilePath/ResourceReport.txt 
		$VBoxMngPath/VBoxManage guestcontrol $i exec --image "$CommandPath1/vmstat" --username $Username --password $Password --wait-stdout | tee -a $ResUtilFilePath/ResourceReport.txt 
		echo $'\n\n'| tee -a $ResUtilFilePath/ResourceReport.txt 
		echo $'---RAM Usage:---\n'| tee -a $ResUtilFilePath/ResourceReport.txt 
		$VBoxMngPath/VBoxManage guestcontrol $i exec --image "$CommandPath1/free" --username $Username --password $Password --wait-stdout | tee -a $ResUtilFilePath/ResourceReport.txt 
		echo $'\n\n'| tee -a $ResUtilFilePath/ResourceReport.txt 
		echo $'---Disk Usage:---\n'| tee -a $ResUtilFilePath/ResourceReport.txt 
		$VBoxMngPath/VBoxManage guestcontrol $i exec --image "$CommandPath2/df" --username $Username --password $Password --wait-stdout -- "-h" | tee -a $ResUtilFilePath/ResourceReport.txt 
		echo $'\n\n'| tee -a $ResUtilFilePath/ResourceReport.txt 
		echo $'---I/O Statistics:---\n'| tee -a $ResUtilFilePath/ResourceReport.txt
		$VBoxMngPath/VBoxManage guestcontrol $i exec --image "$CommandPath1/iostat" --username $Username --password $Password --wait-stdout | tee -a $ResUtilFilePath/ResourceReport.txt 
		echo $'\n\n'
		
	fi	
	done
}

function displayOperations(){
echo ""	
echo "----Select the operation you want to perform---"
echo "1) Taking snapshot of all VMs"
echo "2) Obtain current resource utilization of all VMs" 
echo "3) Create text files" 
echo "4) Delete all .txt files in a folder" 

##### Select Operation
echo -n "----Select Operation ID: "
read OperationID

##### Call selected operation function
case $OperationID in
	1) snap ;;
	2) resutil ;;
	3) createTxtFiles ;;
	4) deleteTxt ;;
	*) echo "INVALID CHOICE!" ;;
esac
}

##############################  MAIN
displayVms
displayOperations