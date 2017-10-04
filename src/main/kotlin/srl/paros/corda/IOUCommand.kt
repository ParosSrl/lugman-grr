package srl.paros.corda

import net.corda.core.contracts.CommandData

interface IOUCreate : CommandData

class PlainIOUCreate : IOUCreate