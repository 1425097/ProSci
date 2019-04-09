package at.ac.tuwien.ifs.prosci.provstarter.helper;

import org.springframework.stereotype.Component;
import oshi.hardware.*;
import oshi.hardware.CentralProcessor.TickType;
import oshi.software.os.*;
import oshi.software.os.OperatingSystem.ProcessSort;
import oshi.util.FormatUtil;
import oshi.util.Util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class InfoCollector {

    private static FileWriter writer;
    private File file;

    protected static void printComputerSystem(final ComputerSystem computerSystem) throws IOException {
        writer.write("+++++++++++++++++++\n" );
        writer.write("+ Computer System +\n");
        writer.write("+++++++++++++++++++\n");
        writer.write("manufacturer: " + computerSystem.getManufacturer() + "\n");
        writer.write("model: " + computerSystem.getModel() + "\n");
        writer.write("serialnumber: " + computerSystem.getSerialNumber() + "\n");
        final Firmware firmware = computerSystem.getFirmware();
        writer.write("firmware:" + "\n");
        writer.write("  manufacturer: " + firmware.getManufacturer() + "\n");
        writer.write("  name: " + firmware.getName() + "\n");
        writer.write("  description: " + firmware.getDescription() + "\n");
        writer.write("  version: " + firmware.getVersion() + "\n");
        writer.write("  release date: " + (firmware.getReleaseDate() == null ? "unknown"
                : firmware.getReleaseDate() == null ? "unknown" : FormatUtil.formatDate(firmware.getReleaseDate())) + "\n");
        final Baseboard baseboard = computerSystem.getBaseboard();
        writer.write("baseboard:" + "\n");
        writer.write("  manufacturer: " + baseboard.getManufacturer() + "\n");
        writer.write("  model: " + baseboard.getModel() + "\n");
        writer.write("  version: " + baseboard.getVersion() + "\n");
        writer.write("  serialnumber: " + baseboard.getSerialNumber() + "\n");
    }

    protected static void printProcessor(CentralProcessor processor) throws IOException {
        writer.write("+++++++++++++++++++\n" );
        writer.write("+    Processor    +\n");
        writer.write("+++++++++++++++++++\n");
        writer.write("" + processor + "\n");
        writer.write(" " + processor.getPhysicalProcessorCount() + " physical CPU(s)" + "\n");
        writer.write(" " + processor.getLogicalProcessorCount() + " logical CPU(s)" + "\n");

        writer.write("Identifier: " + processor.getIdentifier() + "\n");
        writer.write("ProcessorID: " + processor.getProcessorID() + "\n");
    }

    protected static void printMemory(GlobalMemory memory) throws IOException {
        writer.write("+++++++++++++++++++\n" );
        writer.write("+      Memory     +\n");
        writer.write("+++++++++++++++++++\n");
        writer.write("Memory: " + FormatUtil.formatBytes(memory.getAvailable()) + "/"
                + FormatUtil.formatBytes(memory.getTotal()) + "\n");
        writer.write("Swap used: " + FormatUtil.formatBytes(memory.getSwapUsed()) + "/"
                + FormatUtil.formatBytes(memory.getSwapTotal()) + "\n");
    }

    protected static void printCpu(CentralProcessor processor) throws IOException {
        writer.write("+++++++++++++++++++\n" );
        writer.write("+        Cpu      +\n");
        writer.write("+++++++++++++++++++\n");
        writer.write("Uptime: " + FormatUtil.formatElapsedSecs(processor.getSystemUptime()) + "\n");

        long[] prevTicks = processor.getSystemCpuLoadTicks();
        writer.write("CPU, IOWait, and IRQ ticks @ 0 sec:" + Arrays.toString(prevTicks) + "\n");
        // Wait a second...
        Util.sleep(1000);
        long[] ticks = processor.getSystemCpuLoadTicks();
        writer.write("CPU, IOWait, and IRQ ticks @ 1 sec:" + Arrays.toString(ticks) + "\n");
        long user = ticks[TickType.USER.getIndex()] - prevTicks[TickType.USER.getIndex()];
        long nice = ticks[TickType.NICE.getIndex()] - prevTicks[TickType.NICE.getIndex()];
        long sys = ticks[TickType.SYSTEM.getIndex()] - prevTicks[TickType.SYSTEM.getIndex()];
        long idle = ticks[TickType.IDLE.getIndex()] - prevTicks[TickType.IDLE.getIndex()];
        long iowait = ticks[TickType.IOWAIT.getIndex()] - prevTicks[TickType.IOWAIT.getIndex()];
        long irq = ticks[TickType.IRQ.getIndex()] - prevTicks[TickType.IRQ.getIndex()];
        long softirq = ticks[TickType.SOFTIRQ.getIndex()] - prevTicks[TickType.SOFTIRQ.getIndex()];
        long steal = ticks[TickType.STEAL.getIndex()] - prevTicks[TickType.STEAL.getIndex()];
        long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;

        writer.write(String.format(
                "User: %.1f%% Nice: %.1f%% System: %.1f%% Idle: %.1f%% IOwait: %.1f%% IRQ: %.1f%% SoftIRQ: %.1f%% Steal: %.1f%%%n",
                100d * user / totalCpu, 100d * nice / totalCpu, 100d * sys / totalCpu, 100d * idle / totalCpu,
                100d * iowait / totalCpu, 100d * irq / totalCpu, 100d * softirq / totalCpu, 100d * steal / totalCpu));
        writer.write(String.format("CPU load: %.1f%% (counting ticks)%n", processor.getSystemCpuLoadBetweenTicks() * 100));
        writer.write(String.format("CPU load: %.1f%% (OS MXBean)%n", processor.getSystemCpuLoad() * 100));
        double[] loadAverage = processor.getSystemLoadAverage(3);
        writer.write("CPU load averages:" + (loadAverage[0] < 0 ? " N/A" : String.format(" %.2f", loadAverage[0]))
                + (loadAverage[1] < 0 ? " N/A" : String.format(" %.2f", loadAverage[1]))
                + (loadAverage[2] < 0 ? " N/A" : String.format(" %.2f", loadAverage[2])) + "\n");
        // per core CPU
        StringBuilder procCpu = new StringBuilder("CPU load per processor:");
        double[] load = processor.getProcessorCpuLoadBetweenTicks();
        for (double avg : load) {
            procCpu.append(String.format(" %.1f%%", avg * 100));
        }
        writer.write(procCpu.toString() + "\n");
    }

    protected static void printProcesses(OperatingSystem os, GlobalMemory memory) throws IOException {
        writer.write("+++++++++++++++++++\n" );
        writer.write("+     Processes   +\n");
        writer.write("+++++++++++++++++++\n");
        writer.write("Processes: " + os.getProcessCount() + ", Threads: " + os.getThreadCount() + "\n");
        // Sort by highest CPU
        List<OSProcess> procs = Arrays.asList(os.getProcesses(5, ProcessSort.CPU));

        writer.write("   PID  %CPU %MEM       VSZ       RSS Name" + "\n");
        for (int i = 0; i < procs.size() && i < 5; i++) {
            OSProcess p = procs.get(i);
            writer.write(String.format(" %5d %5.1f %4.1f %9s %9s %s%n", p.getProcessID(),
                    100d * (p.getKernelTime() + p.getUserTime()) / p.getUpTime(),
                    100d * p.getResidentSetSize() / memory.getTotal(), FormatUtil.formatBytes(p.getVirtualSize()),
                    FormatUtil.formatBytes(p.getResidentSetSize()), p.getName()));
        }
    }

    protected static void printSensors(Sensors sensors) throws IOException {
        writer.write("+++++++++++++++++++\n" );
        writer.write("+     Sensors     +\n");
        writer.write("+++++++++++++++++++\n");
        writer.write("Sensors:" + "\n");
        writer.write(String.format(" CPU Temperature: %.1f°C%n", sensors.getCpuTemperature()));
        writer.write(" Fan Speeds: " + Arrays.toString(sensors.getFanSpeeds()) + "\n");
        writer.write(String.format(" CPU Voltage: %.1fV%n", sensors.getCpuVoltage()));
    }

    protected static void printPowerSources(PowerSource[] powerSources) throws IOException {
        writer.write("+++++++++++++++++++\n" );
        writer.write("+  Power Sources  +\n");
        writer.write("+++++++++++++++++++\n");
        StringBuilder sb = new StringBuilder("Power: ");
        if (powerSources.length == 0) {
            sb.append("Unknown");
        } else {
            double timeRemaining = powerSources[0].getTimeRemaining();
            if (timeRemaining < -1d) {
                sb.append("Charging");
            } else if (timeRemaining < 0d) {
                sb.append("Calculating time remaining");
            } else {
                sb.append(String.format("%d:%02d remaining", (int) (timeRemaining / 3600),
                        (int) (timeRemaining / 60) % 60));
            }
        }
        for (PowerSource pSource : powerSources) {
            sb.append(String.format("%n %s @ %.1f%%", pSource.getName(), pSource.getRemainingCapacity() * 100d));
        }
        writer.write(sb.toString() + "\n");
    }

    protected static void printDisks(HWDiskStore[] diskStores) throws IOException {
        writer.write("+++++++++++++++++++\n" );
        writer.write("+      Disks      +\n");
        writer.write("+++++++++++++++++++\n");
        for (HWDiskStore disk : diskStores) {
            boolean readwrite = disk.getReads() > 0 || disk.getWrites() > 0;
            writer.write(String.format(" %s: (model: %s - S/N: %s) size: %s, reads: %s (%s), writes: %s (%s), xfer: %s ms%n",
                    disk.getName(), disk.getModel(), disk.getSerial(),
                    disk.getSize() > 0 ? FormatUtil.formatBytesDecimal(disk.getSize()) : "?",
                    readwrite ? disk.getReads() : "?", readwrite ? FormatUtil.formatBytes(disk.getReadBytes()) : "?",
                    readwrite ? disk.getWrites() : "?", readwrite ? FormatUtil.formatBytes(disk.getWriteBytes()) : "?",
                    readwrite ? disk.getTransferTime() : "?"));
            HWPartition[] partitions = disk.getPartitions();
            if (partitions == null) {
                // TODO Remove when all OS's implemented
                continue;
            }
            for (HWPartition part : partitions) {
                writer.write(String.format(" |-- %s: %s (%s) Maj:Min=%d:%d, size: %s%s%n", part.getIdentification(),
                        part.getName(), part.getType(), part.getMajor(), part.getMinor(),
                        FormatUtil.formatBytesDecimal(part.getSize()),
                        part.getMountPoint().isEmpty() ? "" : " @ " + part.getMountPoint()));
            }
        }
    }

    protected static void printFileSystem(FileSystem fileSystem) throws IOException {
        writer.write("+++++++++++++++++++\n" );
        writer.write("+   FileSystem    +\n");
        writer.write("+++++++++++++++++++\n");

        writer.write(String.format(" File Descriptors: %d/%d%n", fileSystem.getOpenFileDescriptors(),
                fileSystem.getMaxFileDescriptors()));

        OSFileStore[] fsArray = fileSystem.getFileStores();
        for (OSFileStore fs : fsArray) {
            long usable = fs.getUsableSpace();
            long total = fs.getTotalSpace();
            writer.write(String.format(" %s (%s) [%s] %s of %s free (%.1f%%) is %s " +
                            (fs.getLogicalVolume() != null && fs.getLogicalVolume().length() > 0 ? "[%s]" : "%s") +
                            " and is mounted at %s%n", fs.getName(),
                    fs.getDescription().isEmpty() ? "file system" : fs.getDescription(), fs.getType(),
                    FormatUtil.formatBytes(usable), FormatUtil.formatBytes(fs.getTotalSpace()), 100d * usable / total,
                    fs.getVolume(), fs.getLogicalVolume(), fs.getMount()));
        }
    }

    protected static void printNetworkInterfaces(NetworkIF[] networkIFs) throws IOException {
        writer.write("++++++++++++++++++++\n" );
        writer.write("+Network interfaces+\n");
        writer.write("++++++++++++++++++++\n");

        for (NetworkIF net : networkIFs) {
            writer.write(String.format(" Name: %s (%s)%n", net.getName(), net.getDisplayName()));
            writer.write(String.format("   MAC Address: %s %n", net.getMacaddr()));
            writer.write(String.format("   MTU: %s, Speed: %s %n", net.getMTU(), FormatUtil.formatValue(net.getSpeed(), "bps")));
            writer.write(String.format("   IPv4: %s %n", Arrays.toString(net.getIPv4addr())));
            writer.write(String.format("   IPv6: %s %n", Arrays.toString(net.getIPv6addr())));
            boolean hasData = net.getBytesRecv() > 0 || net.getBytesSent() > 0 || net.getPacketsRecv() > 0
                    || net.getPacketsSent() > 0;
            writer.write(String.format("   Traffic: received %s/%s%s; transmitted %s/%s%s %n",
                    hasData ? net.getPacketsRecv() + " packets" : "?",
                    hasData ? FormatUtil.formatBytes(net.getBytesRecv()) : "?",
                    hasData ? " (" + net.getInErrors() + " err)" : "",
                    hasData ? net.getPacketsSent() + " packets" : "?",
                    hasData ? FormatUtil.formatBytes(net.getBytesSent()) : "?",
                    hasData ? " (" + net.getOutErrors() + " err)" : ""));
        }
    }

    protected static void printNetworkParameters(NetworkParams networkParams) throws IOException {
        writer.write("++++++++++++++++++++\n" );
        writer.write("+Network parameters+\n");
        writer.write("++++++++++++++++++++\n");

        writer.write(String.format(" Host name: %s%n", networkParams.getHostName()));
        writer.write(String.format(" Domain name: %s%n", networkParams.getDomainName()));
        writer.write(String.format(" DNS servers: %s%n", Arrays.toString(networkParams.getDnsServers())));
        writer.write(String.format(" IPv4 Gateway: %s%n", networkParams.getIpv4DefaultGateway()));
        writer.write(String.format(" IPv6 Gateway: %s%n", networkParams.getIpv6DefaultGateway()));
    }

    protected static void printDisplays(Display[] displays) throws IOException {
        writer.write("++++++++++++++++++++\n" );
        writer.write("+     Displays     +\n");
        writer.write("++++++++++++++++++++\n");

        int i = 0;
        for (Display display : displays) {
            writer.write(String.format(" Display " + i + ":\n"));
            writer.write(String.format(display.toString() + "\n"));
            i++;
        }
    }

    protected static void printUsbDevices(UsbDevice[] usbDevices) throws IOException {
        writer.write("++++++++++++++++++++\n" );
        writer.write("+    USB Devices   +\n");
        writer.write("++++++++++++++++++++\n");
        for (UsbDevice usbDevice : usbDevices) {
            writer.write(String.format(usbDevice.toString() + "\n"));
        }
    }

    protected void setFile(File file) throws IOException {
        this.file = file;
        this.writer = new FileWriter(file, true);
    }

    protected void close() throws IOException {
        writer.close();
    }
}