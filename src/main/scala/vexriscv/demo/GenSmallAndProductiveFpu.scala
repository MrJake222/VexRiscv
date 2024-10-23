package vexriscv.demo

import vexriscv.plugin._
import vexriscv.{plugin, VexRiscv, VexRiscvConfig}
import spinal.core._
import vexriscv.ip.fpu._
import vexriscv.ip.{DataCacheConfig}

/**
 * Created by spinalvm & mrjake222
 */
object GenSmallAndProductiveMyFpu extends App{
  def smallest_my(mtvecInit : BigInt)  = CsrPluginConfig(
    catchIllegalAccess = false,
    mvendorid      = null,
    marchid        = null,
    mimpid         = null,
    mhartid        = null,
    misaExtensionsInit = 66,
    misaAccess     = CsrAccess.NONE,
    mtvecAccess    = CsrAccess.NONE,
    mtvecInit      = mtvecInit,
    mepcAccess     = CsrAccess.NONE,
    mscratchGen    = false,
    mcauseAccess   = CsrAccess.READ_ONLY,
    mbadaddrAccess = CsrAccess.NONE,
    mcycleAccess   = CsrAccess.NONE,
    minstretAccess = CsrAccess.NONE,
    ecallGen       = false,
    wfiGenAsWait         = false,
    ucycleAccess   = CsrAccess.READ_ONLY,
    uinstretAccess = CsrAccess.READ_ONLY
  )
    
  def cpu() = new VexRiscv(
    config = VexRiscvConfig(
      plugins = List(
        new IBusSimplePlugin(
          resetVector = 0x00020000l,
          cmdForkOnSecondStage = false,
          cmdForkPersistence = true,
          prediction = NONE,
          catchAccessFault = false,
          compressedGen = false
        ),
	new DBusCachedPlugin(
            config = new DataCacheConfig(
              cacheSize         = 4096,
              bytePerLine       = 32,
              wayCount          = 1,
              addressWidth      = 32,
              cpuDataWidth      = 32,
              memDataWidth      = 32,
              catchAccessError  = true,
              catchIllegal      = true,
              catchUnaligned    = true
           ),
           memoryTranslatorPortConfig = null
         ),
        new StaticMemoryTranslatorPlugin(
            ioRange      = _(31 downto 28) === 0xF
        ),
	new CsrPlugin(smallest_my(0x00000020l)),
        new DecoderSimplePlugin(
          catchIllegalInstruction = true
        ),
        new RegFilePlugin(
          regFileReadyKind = plugin.SYNC,
          zeroBoot = false
        ),
        new IntAluPlugin,
        new SrcPlugin(
          separatedAddSub = false,
          executeInsertion = true
        ),
        new LightShifterPlugin,
        new HazardSimplePlugin(
          bypassExecute           = true,
          bypassMemory            = true,
          bypassWriteBack         = true,
          bypassWriteBackBuffer   = true,
          pessimisticUseSrc       = false,
          pessimisticWriteRegFile = false,
          pessimisticAddressMatch = false
        ),
	new FpuPlugin(
            externalFpu = false,
            p = FpuParameter(
              withDouble = false
            )
	),
        new BranchPlugin(
          earlyBranch = false,
          catchAddressMisaligned = false
        ),
        new YamlPlugin("cpu0.yaml")
      )
    )
  )

  SpinalVerilog(cpu())
}
