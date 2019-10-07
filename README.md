# VeriBlock Integration Service
## Overview
Altchains adopting Bitcoin security through VeriBlock will use the VeriBlock Integration Point
to record and maintain SPV-level knowledge of VeriBlock (and by extension, Bitcoin). VTB
payloads contained in PoP transactions on the altchain are
provided to the VeriBlock integration point, from which it constructs the SPV view. During
fork resolution over a keystone boundary, the altchain will consult the integration point to
determine the timeliness of publications of its keystone periods to the VeriBlock blockchain.

During normal operation, the integration point maintains a deterministic SPV-level view of
VeriBlock based entirely on the VTB publications it has been provided. It maintains an
association of these VTB payloads with their enclosing altchain block (by block number), so
that altchain reorganizations can undo the state changes the unapplied blocks made to the
integration point’s view of VeriBlock.

During fork resolution, the integration point is temporarily updated with VTB information
contained within the challenging fork to ensure the integration point’s view includes
information stored in both forks. After fork resolution, the altchain sends a command to the
integration point to clear the temporarily-added VTB payloads, which returns the integration
point’s view to one which only reflects VTB payloads in the main chain. In the event that the
challenging fork is selected, the altchain still sends the clear command, and then un-applies
VTB payloads in the previously-main chain back to the forking point, and applies the new
VTB payloads from the new fork.

Appendix B of the document [Proof-of-Proof and VeriBlock Blockchain Protocol Consensus Algorithm and Economic Incentivization](https://mirror1.veriblock.org/Proof-of-Proof_and_VeriBlock_Blockchain_Protocol_Consensus_Algorithm_and_Economic_Incentivization_v1.0.pdf) contains details about the library's API and expected uses.