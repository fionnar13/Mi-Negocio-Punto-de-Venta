package com.elfrikiamv.minegocio_puntodeventa.dao.shopping

// TicketDao.kt

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.elfrikiamv.minegocio_puntodeventa.model.shopping.TicketEntity

@Dao
interface TicketDao {
    @Insert
    suspend fun insertTicket(ticket: TicketEntity)

    @Query("SELECT * FROM tickets ORDER BY ticketId DESC")
    suspend fun getAllTickets(): List<TicketEntity>

    @Query("DELETE FROM tickets WHERE ticketId = :ticketId")
    suspend fun deleteTicketById(ticketId: String)

}